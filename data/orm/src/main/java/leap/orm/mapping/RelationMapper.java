/*
 * Copyright 2014 the original author or authors.
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
package leap.orm.mapping;

import java.util.ArrayList;
import java.util.List;

import leap.db.model.DbForeignKeyBuilder;
import leap.db.model.DbForeignKeyColumn;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.orm.annotation.Relational;
import leap.orm.metadata.MetadataException;
import leap.orm.naming.NamingStrategy;

public class RelationMapper implements Mapper {

	@Override
	public void completeMappings(MappingConfigContext context) throws MetadataException {
		for(EntityMappingBuilder emb : context.getEntityMappings()){
			processRelationMappings(context, emb);
		}

        for(EntityMappingBuilder emb : context.getEntityMappings()) {
            processInverseRelations(context, emb);
        }

        for(EntityMappingBuilder emb : context.getEntityMappings()) {
            processRelationProperties(context, emb);
        }
	}

	protected void processRelationMappings(MappingConfigContext context,EntityMappingBuilder emb) {
		for(RelationMappingBuilder rmb : emb.getRelationMappings()){
			processRelationMapping(context, emb, rmb);
		}
	}

    protected void processInverseRelations(MappingConfigContext context, EntityMappingBuilder emb) {
        List<RelationMappingBuilder> keyRelations = new ArrayList<>();

        //Avoid the java.util.ConcurrentModificationException in relation mapping list.
        List<RelationMappingBuilder> relationSnapshot = new ArrayList<>(emb.getRelationMappings());

        for(RelationMappingBuilder rmb : relationSnapshot) {
            //Be careful, the target entity may be the self entity of local entity, so we use snapshot list
            EntityMappingBuilder targetEmb = context.getEntityMapping(rmb.getTargetEntityName());

            RelationType type = rmb.getType();

            //check is a key reference of target entity (reference to the id fields).
            if(RelationType.MANY_TO_ONE.equals(type)) {

                boolean isAllIdFields = true;

                for(JoinFieldMappingBuilder jf : rmb.getJoinFields()) {
                    FieldMappingBuilder rf = targetEmb.findFieldMappingByName(jf.getReferencedFieldName());
                    if(!rf.isId()) {
                        isAllIdFields = false;
                        break;
                    }
                }

                if(isAllIdFields) {
                    keyRelations.add(rmb);
                }
            }

            //create one-to-many for many-to-one
            if(RelationType.MANY_TO_ONE.equals(type)) {
                //find one-to-many in target entity.
                RelationMappingBuilder inverse = findRelation(targetEmb, emb, RelationType.ONE_TO_MANY, rmb.getInverseRelationName());
                if(null == inverse) {
                    //create the virtual inverse one-to-many relation in target entity.
                    targetEmb.addRelationMapping(createInverseOneToManyRelation(emb, targetEmb, rmb));
                }else{
                    inverse.setInverseRelationName(rmb.getName());
                    rmb.setInverseRelationName(inverse.getName());
                }
                continue;
            }

            //create many-to-many for another side.
            if(RelationType.MANY_TO_MANY.equals(type)) {
                RelationMappingBuilder inverse = findRelation(targetEmb, emb, RelationType.MANY_TO_MANY, rmb.getInverseRelationName());
                if(null == inverse) {
                    //create the virtual inverse relation in target entity.
                    targetEmb.addRelationMapping(createInverseManyToManyRelation(emb, targetEmb, rmb));
                }else{
                    inverse.setInverseRelationName(rmb.getName());
                    rmb.setInverseRelationName(inverse.getName());
                }
                continue;
            }
        }

        //create many-to-many by join entity.
        if(emb.getKeyFieldMappings().size() > 1 && keyRelations.size() == 2) {
            int keySize = 0;
            for(RelationMappingBuilder r : keyRelations) {
                keySize += r.getJoinFields().size();
            }

            if(emb.getKeyFieldMappings().size() == keySize) {
                EntityMappingBuilder entity1 = context.getEntityMapping(keyRelations.get(0).getTargetEntityName());
                EntityMappingBuilder entity2 = context.getEntityMapping(keyRelations.get(1).getTargetEntityName());

                RelationMappingBuilder manyToMany1 =
                        findManyToManyRelation(entity1, entity2.getEntityName(), emb.getEntityName());

                RelationMappingBuilder manyToMany2 =
                        findManyToManyRelation(entity2, entity1.getEntityName(), emb.getEntityName());

                if(null == manyToMany1) {
                    manyToMany1 = createManyToManyRelation(entity1, entity2, emb);
                    entity1.addRelationMapping(manyToMany1);
                }

                if(null == manyToMany2) {
                    manyToMany2 = createManyToManyRelation(entity2, entity1, emb);
                    entity2.addRelationMapping(manyToMany2);
                }

                manyToMany1.setInverseRelationName(manyToMany2.getName());
                manyToMany2.setInverseRelationName(manyToMany1.getName());
            }
        }
    }

    protected RelationMappingBuilder findManyToManyRelation(EntityMappingBuilder emb, String targetEntityName, String joinEntityName) {

        for(RelationMappingBuilder rm : emb.getRelationMappings()) {

            if(rm.getType().equals(RelationType.MANY_TO_MANY) &&
                    rm.getTargetEntityName().equals(targetEntityName) &&
                    rm.getJoinEntityName().equals(joinEntityName)) {

                return rm;

            }

        }

        return null;
    }

	protected void processRelationMapping(MappingConfigContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb) {
		RelationType type = rmb.getType();
		
		if(type == RelationType.MANY_TO_ONE){
			processManyToOneMapping(context, emb, rmb);
			return;
		}
		
		if(type == RelationType.MANY_TO_MANY) {
			processManyToManyMapping(context, emb, rmb);
			return;
		}
		
		throw new MappingConfigException("Relation type '" + type + "' defines at entity '" + emb.getEntityName() + "' does not supports now");
	}
	
	protected void processManyToOneMapping(MappingConfigContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb) {
		//check target entity exists
		EntityMappingBuilder targetEmb = verifyTargetEntity(context, emb, rmb);

		//resolve relation's name
		if(Strings.isEmpty(rmb.getName())){
			rmb.setName(targetEmb.getEntityName());
		}
		
		//resolve referenced fields
		List<FieldMappingBuilder> referencedFields = targetEmb.getKeyFieldMappings();
		if(referencedFields.isEmpty()){
			throw new MetadataException("Cannot create ManyToOne relation on entity '" + emb.getEntityName() + 
										"', target entity '" + targetEmb.getEntityName() + "' must defines key fields");
		}
		
		if(!rmb.getJoinFields().isEmpty()){
			verifyManyToOneJoinFields(context, emb, targetEmb, referencedFields, rmb);
		}else{
			createManyToOneJoinFields(context, emb, targetEmb, referencedFields, rmb);
		}
		
		//Auto create local fields
		for(JoinFieldMappingBuilder joinField : rmb.getJoinFields()) {

			FieldMappingBuilder localField = emb.findFieldMappingByName(joinField.getLocalFieldName());
			if(null == localField){
				localField = createManyToOneLocalField(context, emb, targetEmb, rmb, joinField);
			}else{
				updateManyToOneLocalField(context, emb, targetEmb, rmb, joinField, localField);
			}

            if(null == rmb.getOptional() && localField.isNullable()) {
                rmb.setOptional(true);
            }

            if(localField.isId()) {
                joinField.setLocalPrimaryKey(true);
            }else{
                localField.setNullable(rmb.isOptional());
                localField.getColumn().setNullable(rmb.isOptional());
            }
		}

		//create foreign key
		createManyToOneForeignKey(context, emb, targetEmb, rmb);
	}
	
	protected void processManyToManyMapping(MappingConfigContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb) {
		//check target entity exists
		EntityMappingBuilder targetEmb = verifyTargetEntity(context, emb, rmb);
		
		//resolve relation's name
		if(Strings.isEmpty(rmb.getName())){
			rmb.setName(targetEmb.getEntityName());
		}
		
		EntityMappingBuilder joinEmb = verifyJoinEntity(context, emb, targetEmb, rmb);
		if(null == joinEmb){
			//create join entity
			createManyToManyJoinEntity(context, emb, targetEmb, rmb);
		}else{
			//verify join entity
			verifyManyToManyJoinEntity(context, emb, targetEmb, joinEmb, rmb);
		}
	}
	
	protected EntityMappingBuilder verifyTargetEntity(MappingConfigContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb) {
		//check target entity exists
		EntityMappingBuilder targetEmb = null;
		
		if(null != rmb.getTargetEntityType()){
			targetEmb = context.tryGetEntityMapping(rmb.getTargetEntityType());
			
			if(null == targetEmb){
				throw new MetadataException("No entity mapping to targetEntityType '" + rmb.getTargetEntityType() + "' defines at entity '" + emb.getEntityName() + "'");
			}
			
			rmb.setTargetEntityName(targetEmb.getEntityName());

		}else if(!Strings.isEmpty(rmb.getTargetEntityName())){
			targetEmb = context.tryGetEntityMapping(rmb.getTargetEntityName());
			
			if(null == targetEmb){
				throw new MetadataException("No entity mapping to targetEntityName '" + rmb.getTargetEntityName() + "' defines at entity '" + emb.getEntityName() + "'");
			}
		}

		return targetEmb;
	}
	
	protected EntityMappingBuilder verifyJoinEntity(MappingConfigContext context,EntityMappingBuilder emb,EntityMappingBuilder targetEmb, RelationMappingBuilder rmb) {
		//check join entity exists
		EntityMappingBuilder joinEmb = null;
		
		if(null != rmb.joinEntityType){
			joinEmb = context.tryGetEntityMapping(rmb.getJoinEntityType());
			
			if(null == joinEmb){
				throw new MetadataException("No entity mapping to joinEntityType '" + rmb.getJoinEntityType() + "' defines at entity '" + emb.getEntityName() + "'");
			}
			
			rmb.setJoinEntityName(joinEmb.getEntityName());

		}else if(!Strings.isEmpty(rmb.getJoinEntityName())){
			joinEmb = context.tryGetEntityMapping(rmb.getJoinEntityName());
		}
		
		if(null == joinEmb) {
			for(RelationMappingBuilder trmb : targetEmb.getRelationMappings()) {
				if(trmb.getType() == RelationType.MANY_TO_MANY) {
					if(trmb.getTargetEntityType().equals(emb.getEntityClass()) || 
					   trmb.getTargetEntityName().equals(emb.getEntityName())) {
						
						if(null != trmb.joinEntityType){
							return context.tryGetEntityMapping(trmb.getJoinEntityType());
						}else if(!Strings.isEmpty(rmb.getJoinEntityName())){
							return context.tryGetEntityMapping(rmb.getJoinEntityName());
						}	

						return null;
					}
				}
			}
		}

		return joinEmb;
	}
	
	protected void verifyManyToOneJoinFields(MappingConfigContext context,
											 EntityMappingBuilder emb, EntityMappingBuilder targetEmb, 
											 List<FieldMappingBuilder> referencedFields,  RelationMappingBuilder rmb) {
		
		List<JoinFieldMappingBuilder> joinFields = rmb.getJoinFields();
		
		if(joinFields.size() != referencedFields.size()) {
			throw new MetadataException("The size of 'JoinField' must equals to size of key fields in target entity '" + targetEmb.getEntityName() + "'");
		}
		
		for(JoinFieldMappingBuilder jf : joinFields) {
			
			if(Strings.isEmpty(jf.getReferencedFieldName())){
				if(referencedFields.size() == 1) {
					jf.setReferencedFieldName(referencedFields.get(0).getFieldName());
				}else {
					//guess the referenced name
					String referencedFieldName = null;
					for(FieldMappingBuilder ref : referencedFields){
						if(jf.getLocalFieldName().endsWith(Strings.upperFirst(ref.getFieldName()))){
							referencedFieldName = ref.getFieldName();
							break;
						}
					}
					if(null == referencedFieldName){
						throw new MetadataException("Cannot determinate the referenced field name of join field '" + 
												   jf.getLocalFieldName() + "' in entity '" + emb.getEntityName() + "'");
					}
					jf.setReferencedFieldName(referencedFieldName);
				}
			}
			
		}
	}
	
	protected void createManyToOneJoinFields(MappingConfigContext context,
											 EntityMappingBuilder emb, EntityMappingBuilder targetEmb, 
											 List<FieldMappingBuilder> foreignKeyFields, RelationMappingBuilder rmb) {


        if(null != rmb.getBeanProperty()) {
            BeanProperty bp = rmb.getBeanProperty();

            for(FieldMappingBuilder fmb : emb.getFieldMappings()) {

                if(fmb.getBeanProperty() == bp) {

                    if(foreignKeyFields.size() != 1) {
                        throw new MappingConfigException("The key fields of referenced entity '" +
                                                         targetEmb.getEntityName() + "' must be one!");
                    }

                    FieldMappingBuilder referencedField = foreignKeyFields.get(0);

                    JoinFieldMappingBuilder jfmb = new JoinFieldMappingBuilder();
                    jfmb.setLocalFieldName(fmb.getFieldName());
                    jfmb.setReferencedFieldName(referencedField.getFieldName());

                    rmb.addJoinField(jfmb);

                    return;
                }
            }

        }

        NamingStrategy ns = context.getNamingStrategy();

        for(FieldMappingBuilder referencedField : foreignKeyFields) {
            JoinFieldMappingBuilder jfmb = new JoinFieldMappingBuilder();

            jfmb.setLocalFieldName(ns.getLocalFieldName(targetEmb.getEntityName(), referencedField.getFieldName()));
            jfmb.setReferencedFieldName(referencedField.getFieldName());

            rmb.addJoinField(jfmb);
        }
	}
	
	protected FieldMappingBuilder createManyToOneLocalField(MappingConfigContext    context,
											 EntityMappingBuilder    emb,
											 EntityMappingBuilder    targetEmb, 
											 RelationMappingBuilder  rmb, 
											 JoinFieldMappingBuilder jfmb){
		
		FieldMappingBuilder local = context.getMappingStrategy()
										   .createFieldMappingByJoinField(context, emb, targetEmb, rmb, jfmb);
		
		emb.addFieldMapping(local);

        return local;
	}
	
	
	protected void updateManyToOneLocalField(MappingConfigContext    context,
											 EntityMappingBuilder    emb,
											 EntityMappingBuilder    targetEmb, 
											 RelationMappingBuilder  rmb, 
											 JoinFieldMappingBuilder jfmb,
											 FieldMappingBuilder     lfmb){

		context.getMappingStrategy().updateFieldMappingByJoinField(context, emb, targetEmb, rmb, jfmb, lfmb);
	}
	
	protected void createManyToOneForeignKey(MappingConfigContext   context,
											 EntityMappingBuilder   emb,
											 EntityMappingBuilder   targetEmb,
											 RelationMappingBuilder rmb) {
		
		DbForeignKeyBuilder fk = new DbForeignKeyBuilder();
		
		fk.setName(context.getNamingStrategy().getForeignKeyName(emb.getEntityName(), targetEmb.getEntityName(), rmb.getName()));
		fk.setForeignTable(targetEmb.getTableSchemaObjectName());
		
		for(JoinFieldMappingBuilder jfmb : rmb.getJoinFields()) {
			FieldMappingBuilder localField   = emb.findFieldMappingByName(jfmb.getLocalFieldName());
			FieldMappingBuilder foreignField = targetEmb.findFieldMappingByName(jfmb.getReferencedFieldName());
			
			fk.addColumn(new DbForeignKeyColumn(localField.getColumn().getName(), foreignField.getColumn().getName()));
		}
		
		emb.getTable().addForeignKey(fk);
	}
	
	protected void createManyToManyJoinEntity(MappingConfigContext   context,
											  EntityMappingBuilder   emb,
											  EntityMappingBuilder   targetEmb,
											  RelationMappingBuilder rmb) {
		
		String joinEntityName  = rmb.getJoinEntityName();
		String joinTableName   = rmb.getJoinTableName();
		
		if(Strings.isEmpty(joinEntityName)) {
			joinEntityName = context.getNamingStrategy().getJoinEntityName(emb.getEntityName(), targetEmb.getEntityName());
		}
		
		if(Strings.isEmpty(joinTableName)) {
			joinTableName = context.getNamingStrategy().entityToTableName(joinEntityName);
		}
		
		EntityMappingBuilder joinEmb = new EntityMappingBuilder();
		
		joinEmb.setEntityName(joinEntityName);
		joinEmb.setTableCatalog(emb.getTableCatalog());
		joinEmb.setTableSchema(emb.getTableSchema());
		joinEmb.setTablePrefix(emb.getTablePrefix());
		joinEmb.setTableName(joinTableName);
		
		//create many-to-one relations
		RelationMappingBuilder rmb1 = createManyToOneRelationMapping(joinEmb, emb);
		RelationMappingBuilder rmb2 = createManyToOneRelationMapping(joinEmb, targetEmb);
		
		processManyToOneMapping(context, joinEmb, rmb1);
		processManyToOneMapping(context, joinEmb, rmb2);
		
		for(FieldMappingBuilder fmb : joinEmb.getFieldMappings()) {
			fmb.getColumn().setPrimaryKey(true);
		}
		
		rmb.setJoinEntityName(joinEntityName);
		context.addEntityMapping(joinEmb);
	}

    protected RelationMappingBuilder createManyToManyRelation(EntityMappingBuilder emb, EntityMappingBuilder target, EntityMappingBuilder join) {
        RelationMappingBuilder r = new RelationMappingBuilder();

        r.setName(emb.getEntityName() + "_" + join.getEntityName() + "_" + target.getEntityName());
        r.setType(RelationType.MANY_TO_MANY);
        r.setTargetEntity(target);
        r.setJoinEntity(join);

        return r;
    }
	
	protected RelationMappingBuilder createManyToOneRelationMapping(EntityMappingBuilder emb,EntityMappingBuilder targetEmb) {
		RelationMappingBuilder rmb = new RelationMappingBuilder();
		
		rmb.setType(RelationType.MANY_TO_ONE);
		
		rmb.setTargetEntityType(targetEmb.getEntityClass());
		rmb.setTargetEntityName(targetEmb.getEntityName());
		rmb.setOptional(false);
		
		return rmb;
	}

    /**
     * Creates the inverse one-to-many for the target entity.
     */
    protected RelationMappingBuilder createInverseOneToManyRelation(EntityMappingBuilder local,
                                                                    EntityMappingBuilder target,
                                                                    RelationMappingBuilder manyToOne) {

        String name = "inverse_" + manyToOne.getName() + "_" + local.getEntityName();

        manyToOne.setInverseRelationName(name);

        RelationMappingBuilder oneToMany = new RelationMappingBuilder();

        oneToMany.setName(name);
        oneToMany.setInverseRelationName(manyToOne.getName());
        oneToMany.setType(RelationType.ONE_TO_MANY);
        oneToMany.setVirtual(true);
        oneToMany.setTargetEntity(local);

        return oneToMany;
    }

    /**
     * Creates the inverse many-to-many for the target entity.
     */
    protected RelationMappingBuilder createInverseManyToManyRelation(EntityMappingBuilder local,
                                                                     EntityMappingBuilder target,
                                                                     RelationMappingBuilder localManyToMany) {

        String name = "inverse_" + localManyToMany.getName() + "_" + local.getEntityName();

        localManyToMany.setInverseRelationName(name);

        RelationMappingBuilder targetManyToMany = new RelationMappingBuilder();

        targetManyToMany.setName(name);
        targetManyToMany.setInverseRelationName(localManyToMany.getName());
        targetManyToMany.setType(RelationType.MANY_TO_MANY);
        targetManyToMany.setVirtual(true);
        targetManyToMany.setTargetEntity(local);
        targetManyToMany.setJoinEntityName(localManyToMany.getJoinEntityName());
        targetManyToMany.setJoinEntityType(localManyToMany.getJoinEntityType());
        targetManyToMany.setJoinTableName(localManyToMany.getJoinTableName());

        return targetManyToMany;
    }
	
	protected void verifyManyToManyJoinEntity(MappingConfigContext   context,
											  EntityMappingBuilder   emb,
											  EntityMappingBuilder   targetEmb,
											  EntityMappingBuilder   joinEmb,
											  RelationMappingBuilder rmb) {

		//TODO : verify many-to-many join entity
	}

    protected void processRelationProperties(MappingConfigContext context, EntityMappingBuilder emb) {
        if(null == emb.getEntityClass()) {
            return;
        }

        BeanType bt = BeanType.of(emb.getEntityClass());

        for(BeanProperty bp : bt.getProperties()) {
            Relational a = bp.getAnnotation(Relational.class);
            if(null != a) {
                String relation = a.value();

                boolean many = false;
                Class<?> targetClass;

                if(Iterable.class.isAssignableFrom(bp.getType())) {
                    many = true;
                    targetClass = leap.lang.Types.getActualTypeArgument(bp.getGenericType());
                }else{
                    targetClass = bp.getType();
                }

                EntityMappingBuilder targetEntity = context.tryGetEntityMapping(targetClass);
                if(null == targetEntity) {
                    throw new MappingConfigException("The target class '" + targetClass + "' is not an entity");
                }

                RelationPropertyBuilder rp = new RelationPropertyBuilder(bp);
                rp.setName(bp.getName());
                rp.setMany(many);
                rp.setTargetEntityName(targetEntity.getEntityName());

                //Resolve the relation mapping.
                if(many) {
                    resolveToManyRelation(context, emb, targetEntity, rp, relation);
                }else{
                    resolveToOneRelation(context, emb, targetEntity, rp, relation);
                }

                emb.addRelationProperty(rp);
            }
        }
    }

    protected void resolveToManyRelation(MappingConfigContext context,
                                         EntityMappingBuilder emb, EntityMappingBuilder targetEntity,
                                         RelationPropertyBuilder rp, String relation) {
        //find many-to-one relation in local entity
        RelationMappingBuilder rm = findRelation(emb, targetEntity, RelationType.ONE_TO_MANY, relation);
        if(null != rm) {
            rp.setRelationName(rm.getName());
            return;
        }

        //find many-to-many relation in local entity.
        rm = findRelation(emb, targetEntity, RelationType.MANY_TO_MANY, relation);
        if(null != rm) {
            rp.setRelationName(rm.getName());
            setManyToManyJoinEntity(rp, context.getEntityMapping(rm.getJoinEntityName()));
            return ;
        }

        throw new MappingConfigException("No unique to-many relation " + relation + " between entity '" +
                emb.getEntityClass() + "' and target entity '" +
                targetEntity.getEntityName() + "'");
    }

    protected void setManyToManyJoinEntity(RelationPropertyBuilder rp, EntityMappingBuilder joinEntity) {
        rp.setJoinEntityName(joinEntity.getEntityName());
    }

    protected void resolveToOneRelation(MappingConfigContext context,
                                        EntityMappingBuilder emb, EntityMappingBuilder targetEntity,
                                        RelationPropertyBuilder rp, String relation) {

        //find many-to-one in local entity.
        RelationMappingBuilder rm = findRelation(emb,targetEntity, RelationType.MANY_TO_ONE, relation);

        if(null == rm) {
            throw new MappingConfigException("No unique many-to-one relation " + relation + " in entity '" +
                    emb.getEntityClass() + "' for target entity '" +
                    targetEntity.getEntityName() + "'");
        }

        rp.setRelationName(rm.getName());
    }

    protected RelationMappingBuilder findRelation(EntityMappingBuilder emb, EntityMappingBuilder targetEntity, RelationType type, String relation) {
        List<RelationMappingBuilder> rms = new ArrayList<>();
        for(RelationMappingBuilder rm : emb.getRelationMappings()) {
            if(type.equals(rm.getType()) &&
                    rm.getTargetEntityName().equalsIgnoreCase(targetEntity.getEntityName())) {
                rms.add(rm);
            }
        }

        if(rms.isEmpty()) {
            return null;
        }

        if(Strings.isEmpty(relation)) {

            if(rms.size() == 1) {
                return rms.get(0);
            }

        }else{

            for(RelationMappingBuilder rm : rms) {
                if(rm.getName().equalsIgnoreCase(relation)) {
                    return rm;
                }
            }
        }

        return null;
    }
}
