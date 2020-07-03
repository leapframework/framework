/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package leap.orm.command;

import leap.lang.Strings;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.JoinFieldMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.query.CriteriaQuery;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultCascadeDeleteCommand extends AbstractEntityDaoCommand implements CascadeDeleteCommand {

    protected final DefaultDeleteCommand deleteCommand;

    protected String[] relationNames;
    protected boolean  deleteRelationsOnly;

    public DefaultCascadeDeleteCommand(Dao dao, EntityMapping em, Object id) {
        super(dao, em);
        deleteCommand = new DefaultDeleteCommand(dao, em, id);
    }

    @Override
    public CascadeDeleteCommand setRelationNames(String... relations) {
        this.relationNames = relations;
        return this;
    }

    @Override
    public CascadeDeleteCommand setDeleteRelationsOnly(boolean b) {
        this.deleteRelationsOnly = b;
        return this;
    }

    @Override
    public boolean execute() {
        Set<CascadeRelation> cascadeRelations = new TreeSet<>(CascadeRelation.COMPARATOR);
        for (RelationMapping rm : em.getRelationMappings()) {
            if (rm.isOneToMany()) {
                if (null != relationNames && relationNames.length > 0) {
                    boolean found = false;
                    for (String n : relationNames) {
                        if (n.equalsIgnoreCase(rm.getName())) {
                            if (rm.getTargetEntityName().equals(em.getEntityName())) {
                                throw new UnsupportedOperationException("Cannot cascade delete self referencing entity '" + em.getEntityName() + "'");
                            }

                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }
                }

                EntityMapping   target  = context.getMetadata().getEntityMapping(rm.getTargetEntityName());
                RelationMapping inverse = target.getRelationMapping(rm.getInverseRelationName());
                cascadeRelations.add(new CascadeRelation(target, inverse));
            }
        }

        if (cascadeRelations.isEmpty()) {
            return deleteRelationsOnly ? false : deleteCommand.execute() > 0;
        } else {
            Object id = deleteCommand.id;

            AtomicBoolean result = new AtomicBoolean(false);

            dao.doTransaction((conn) -> {

                for (CascadeRelation cr : cascadeRelations) {
                    EntityMapping   target  = cr.entity;
                    RelationMapping inverse = cr.relation;

                    CriteriaQuery query =
                            dao.createCriteriaQuery(target).whereByReference(inverse, id);

                    if (!Strings.isEmpty(inverse.getOnCascadeDeleteFilter())) {
                        query.whereAnd(inverse.getOnCascadeDeleteFilter());
                    }

                    if (inverse.isOptional() && inverse.isSetNullOnCascadeDelete()) {
                        //update null
                        Map<String, Object> fields = new LinkedHashMap<>();

                        for (JoinFieldMapping jf : inverse.getJoinFields()) {
                            fields.put(jf.getLocalFieldName(), null);
                        }

                        query.update(fields);
                    } else {
                        //update null for self referencing relation, such as parentId.
                        if (target.isSelfReferencing()) {

                            for (RelationMapping rm : target.getSelfReferencingRelations()) {
                                Map<String, Object> fields = new LinkedHashMap<>();

                                for (JoinFieldMapping jf : rm.getJoinFields()) {
                                    fields.put(jf.getLocalFieldName(), null);
                                }

                                dao.createCriteriaQuery(target.getEntityName())
                                        .whereByReference(inverse, id)
                                        .update(fields);
                            }
                        }

                        //delete
                        query.delete();
                    }
                }

                if(!deleteRelationsOnly) {
                    result.set(deleteCommand.execute() > 0);
                }
            });

            return result.get();
        }
    }

    protected static final class CascadeRelation {

        private static final Comparator<CascadeRelation> COMPARATOR = (o1, o2) -> {

            //todo : cyclic reference.

            if (o1.entity.isReferenceTo(o2.entity.getEntityName())) {
                return -1;
            }

            return 1;
        };

        private final EntityMapping   entity;
        private final RelationMapping relation;

        public CascadeRelation(EntityMapping entity, RelationMapping relation) {
            this.entity = entity;
            this.relation = relation;
        }

    }

}