/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.command;

import leap.lang.Arrays2;
import leap.lang.expression.Expression;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.SqlCommand;
import leap.orm.value.EntityWrapper;

import java.util.ArrayList;
import java.util.List;

public class DefaultBatchUpdateCommand extends AbstractEntityDaoCommand implements BatchUpdateCommand {

    protected final List<EntityWrapper> entities = new ArrayList<>();

    public DefaultBatchUpdateCommand(Dao dao, EntityMapping em, Object[] records) {
        super(dao, em);

        for (Object record : records) {
            entities.add(EntityWrapper.wrap(dao.getOrmContext(), em, record));
        }
    }

    @Override
    public int[] execute() {
        if(entities.isEmpty()) {
            return new int[0];
        }

        prepare();

        String[] fields = entities.get(0).getFieldNames().toArray(Arrays2.EMPTY_STRING_ARRAY);
        if(fields.length == 0) {
            throw new IllegalStateException("No update fields");
        }

        //todo: handle embedded fields.

        final SqlCommand primaryCommand =
                context.getSqlFactory().createUpdateCommand(context, em, fields);

        final SqlCommand secondaryCommand =
                em.hasSecondaryTable() ? context.getSqlFactory().createUpdateCommand(context, em, fields, true) : null;

        if(null == secondaryCommand && null == primaryCommand) {
            return new int[0];
        }

        Object[] records = entities.toArray(new EntityWrapper[entities.size()]);

        if(null == secondaryCommand) {
            return primaryCommand.executeBatchUpdate(this, records);
        }

        if(null == primaryCommand) {
            return secondaryCommand.executeBatchUpdate(this, records);
        }

        return dao.doTransaction((s) -> {
            int[] result = primaryCommand.executeBatchUpdate(this, records);
            secondaryCommand.executeBatchUpdate(this, records);
            return result;
        });
    }

    protected void prepare() {
        for (EntityWrapper entity : entities) {
            for (FieldMapping fm : em.getFieldMappings()) {
                Object value = entity.get(fm.getFieldName());
                if (null == value) {
                    Expression expression = fm.getUpdateValue();
                    if (null != expression) {
                        value = expression.getValue(entity);
                        if (null != value) {
                            entity.set(fm.getFieldName(), value);
                        }
                    }
                }
            }
        }
    }
}
