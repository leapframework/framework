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

import java.util.List;

import leap.db.model.DbForeignKeyBuilder;
import leap.db.model.DbForeignKeyColumn;
import leap.lang.Strings;
import leap.orm.metadata.MetadataException;
import leap.orm.naming.NamingStrategy;

public class RelationMapper implements Mapper {

	@Override
	public void completeMappings(MappingConfigContext context) throws MetadataException {
		for(EntityMappingBuilder emb : context.getEntityMappings()){
			processRelationMappings(context, emb);
		}
	}


	protected void processRelationMappings(MappingConfigContext context,EntityMappingBuilder emb) {
		for(RelationMappingBuilder rmb : emb.getRelationMappings()){
			processRelationMapping(context, emb, rmb);
		}
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
		
		throw new MetadataException("Relation type '" + type + "' defines at entity '" + emb.getEntityName() + "' does not supports now");
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
				createManyToOneLocalField(context, emb, targetEmb, rmb, joinField);
			}else{
				updateManyToOneLocalField(context, emb, targetEmb, rmb, joinField, localField);
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
		
		NamingStrategy ns = context.getNamingStrategy();
		
		for(FieldMappingBuilder referencedField : foreignKeyFields) {
			JoinFieldMappingBuilder jfmb = new JoinFieldMappingBuilder();
			
			jfmb.setLocalFieldName(ns.getLocalFieldName(targetEmb.getEntityName(), referencedField.getFieldName()));
			jfmb.setReferencedFieldName(referencedField.getFieldName());
			
			rmb.addJoinField(jfmb);
		}
	}
	
	protected void createManyToOneLocalField(MappingConfigContext    context,
											 EntityMappingBuilder    emb,
											 EntityMappingBuilder    targetEmb, 
											 RelationMappingBuilder  rmb, 
											 JoinFieldMappingBuilder jfmb){
		
		FieldMappingBuilder local = context.getMappingStrategy()
										   .createFieldMappingByJoinField(context, emb, targetEmb, rmb, jfmb);
		
		emb.addFieldMapping(local);
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
		
		emb.getTable().addForeignKey(fk.build());
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
	
	protected RelationMappingBuilder createManyToOneRelationMapping(EntityMappingBuilder emb,EntityMappingBuilder targetEmb) {
		RelationMappingBuilder rmb = new RelationMappingBuilder();
		
		rmb.setType(RelationType.MANY_TO_ONE);
		
		rmb.setTargetEntityType(targetEmb.getEntityClass());
		rmb.setTargetEntityName(targetEmb.getEntityName());
		rmb.setOptional(false);
		
		return rmb;
	}
	
	protected void verifyManyToManyJoinEntity(MappingConfigContext   context,
											  EntityMappingBuilder   emb,
											  EntityMappingBuilder   targetEmb,
											  EntityMappingBuilder   joinEmb,
											  RelationMappingBuilder rmb) {

		//TODO : verfiy many-to-many join entity
	}
}
