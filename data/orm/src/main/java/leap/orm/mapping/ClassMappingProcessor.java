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
package leap.orm.mapping;

import java.lang.annotation.Annotation;

import leap.core.BeanFactory;
import leap.core.BeanFactoryAware;
import leap.core.metamodel.ReservedMetaFieldName;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.core.validation.annotations.Required;
import leap.db.model.DbColumnBuilder;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.orm.annotation.ADomain;
import leap.orm.annotation.Column;
import leap.orm.annotation.ColumnType;
import leap.orm.annotation.Domain;
import leap.orm.annotation.Entity;
import leap.orm.annotation.Id;
import leap.orm.annotation.JoinField;
import leap.orm.annotation.ManyToMany;
import leap.orm.annotation.ManyToOne;
import leap.orm.annotation.Table;
import leap.orm.annotation.meta.MetaName;
import leap.orm.domain.Domains;
import leap.orm.domain.EntityDomain;
import leap.orm.domain.FieldDomain;
import leap.orm.generator.IdGenerator;
import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.MetadataException;

public class ClassMappingProcessor extends MappingProcessorAdapter implements MappingProcessor,BeanFactoryAware {
	
	protected @NotNull BeanFactory beanFactory;
	
	@Override
    public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
    }

	@Override
    public void preMappingEntity(MetadataContext context, EntityMappingBuilder emb) throws MetadataException {
		Class<?> sourceClass = emb.getSourceClass();
		if(null != sourceClass){
			mappingEntityTableByAnnotation(context, emb, sourceClass.getAnnotation(Entity.class));
			mappingEntityTableByAnnotation(context, emb, sourceClass.getAnnotation(Table.class));
			mappingEntityTableByDomain(context, emb, sourceClass.getAnnotation(Domain.class));
			mappingManyToOneByClassAnnotation(context, emb, sourceClass.getDeclaredAnnotationsByType(ManyToOne.class));
			mappingManyToManyByClassAnnotation(context, emb, sourceClass.getDeclaredAnnotationsByType(ManyToMany.class));
		}
    }
	
	@Override
    public void preMappingField(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb) throws MetadataException {
		Annotation[] annotations = fmb.getAnnotations();
		if(null != annotations && annotations.length > 0){
			mappingFieldColumnByAnnotation(context, emb, fmb, Classes.getAnnotation(annotations,Column.class));
			mappingFieldColumnByAnnotation(context, emb, fmb, Classes.getAnnotation(annotations,Id.class));
			mappingFieldColumnByDomain(context, emb, fmb, Classes.getAnnotation(annotations,Domain.class));
			mappingFieldColumnByMetaName(context, emb, fmb, Classes.getAnnotation(annotations,MetaName.class));
			mappingFieldColumnByAnnotation(context, emb, fmb, Classes.getAnnotation(annotations,NotEmpty.class));
			mappingFieldColumnByAnnotation(context, emb, fmb, Classes.getAnnotation(annotations,NotNull.class));
			mappingFieldColumnByAnnotation(context, emb, fmb, Classes.getAnnotation(annotations,Required.class));
		}
    }
	
	@Override
    public void postMappingField(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb) throws MetadataException {
		Annotation[] annotations = fmb.getAnnotations();
		if(null != annotations && annotations.length > 0){
			mappingFieldColumnByAnnotation(context, emb, fmb, Classes.getAnnotation(annotations,Id.class));
		}
    }
	
	@Override
    public void preMappingRelation(MetadataContext context, EntityMappingBuilder emb, RelationMappingBuilder rmb) throws MetadataException {
		BeanProperty bp = rmb.getBeanProperty();
		if(null != bp){
			if(mappingManyToOneByPropertyAnnotation(context, emb, rmb, bp, bp.getAnnotation(ManyToOne.class))){
				return;
			}
			
			if(mappingManyToManyByPropertyAnnotation(context, emb, rmb, bp, bp.getAnnotation(ManyToMany.class))){
				return;
			}
		}
	}

	protected void mappingEntityTableByAnnotation(MetadataContext context,EntityMappingBuilder emb,Entity annotation){
		if(null != annotation){
			emb.setEntityName(Strings.firstNotEmpty(annotation.name(),annotation.value()));
			emb.setTableSchema(annotation.schema());
			emb.setTableName(annotation.table());
			emb.setTableNameDeclared(true);
		}
	}
	
	protected void mappingEntityTableByAnnotation(MetadataContext context,EntityMappingBuilder emb,Table annotation){
		if(null != annotation){
			emb.setTableName(annotation.value());
			emb.setTableNameDeclared(true);
		}
	}
	
	protected void mappingEntityTableByDomain(MetadataContext context,EntityMappingBuilder emb,Domain annotation){
		Domains domains = context.getMetadata().domains();
		if(null != annotation){
			emb.setDomain(domains.getEntityDomain(annotation.value()));
		}else{
			Annotation domainAnnotation = Classes.getAnnotationByMetaType(emb.getSourceClass().getAnnotations(),Domain.class);
			if(null != domainAnnotation){
				Domain metaDomain = domainAnnotation.annotationType().getAnnotation(Domain.class);
				if(!Strings.isEmpty(metaDomain.value())){
					emb.setDomain(domains.getEntityDomain(metaDomain.value()));
				}else{
					emb.setDomain(domains.getEntityDomain(domainAnnotation.annotationType().getSimpleName()));
				}
			}
		}
	}
	
	protected boolean mappingFieldColumnByAnnotation(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder f,Column a){
		if(null != a){
			DbColumnBuilder c = f.getColumn();
			
			//column name
			if(!Strings.isEmpty(a.value())) {
				c.setName(a.value());
				f.setColumnNameDeclared(true);
			}
			if(!Strings.isEmpty(a.name())){
				c.setName(a.name());
				f.setColumnNameDeclared(true);
			}
			
			//column type (jdbc type name)
			if(!Strings.isEmpty(a.typeName())){
				c.setTypeName(a.typeName());
			}else if(a.type() != ColumnType.AUTO){
				c.setTypeName(a.type().getTypeName());
			}
			
			//length
			if(a.length() > 0){
				f.setMaxLength(a.length());
				c.setLength(a.length());
			}
			
			//nullable
			if(!a.nullable().isNone()){
				f.setNullable(a.nullable().getValue());
				c.setNullable(a.nullable().getValue());
			}
			
			//precision
			if(a.precision() > 0){
				f.setPrecision(a.precision());
				c.setPrecision(a.precision());
			}
			
			//scale
			if(a.scale() > 0){
				f.setScale(a.scale());
				c.setScale(a.scale());
			}
			
			//unique
			if(!a.unique().isNone()){
				c.setUnique(a.unique().getValue());
			}
			
			//insert
			if(!a.insert().isNone()){
				f.setInsert(a.insert().getValue());
			}
			
			//update
			if(!a.update().isNone()){
				f.setUpdate(a.update().getValue());
			}
			
			//default-value
			if(!Strings.isEmpty(a.defaultValue())) {
			    f.setDefaultValue(a.defaultValue());
			    c.setDefaultValue(a.defaultValue());
			}
			
			//sort order
			if(a.order() != Integer.MIN_VALUE) {
			    f.setSortOrder(a.order());    
			}
			
			return true;
		}
		
		return false;
	}
	
	protected boolean mappingFieldColumnByDomain(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb,Domain a){
		String domainName = null;
		
		Annotation domainAnnotation = null;
		ADomain adomain = null;
		
		if(null != a){
			domainName = a.value();
		}else{
			domainAnnotation = Classes.getAnnotationByMetaType(fmb.getBeanProperty().getAnnotations(),Domain.class);
			if(null != domainAnnotation){
				Domain metaDomain = domainAnnotation.annotationType().getAnnotation(Domain.class);
				if(!Strings.isEmpty(metaDomain.value())){
					domainName = metaDomain.value();
				}else{
					domainName = domainAnnotation.annotationType().getSimpleName();
				}
			}else {
				domainAnnotation = Classes.getAnnotationByMetaType(fmb.getBeanProperty().getAnnotations(),ADomain.class);
				if(null != domainAnnotation){
					adomain = domainAnnotation.annotationType().getAnnotation(ADomain.class);
					domainName = adomain.name();
				}
			}
		}
		
		Domains domains = context.getMetadata().domains();
		
		if(!Strings.isEmpty(domainName)) {
			FieldDomain domain = null;
			
			EntityDomain entityDomain = domains.tryGetEntityDomain(emb.getEntityName());
			
			if(null != entityDomain){
				domain = domains.tryGetFieldDomain(domains.qualifyName(entityDomain.getName(), domainName));
			}
			
			if(null == domain){
				domain = domains.tryGetFieldDomain(domainName);
			}
			
			if(null == domain){
				throw new MappingConfigException("The domain '" + domainName + "' not found, please check the annotation in field '" + 
												 fmb.getBeanProperty().getName() + "' of class '" + emb.getEntityClass().getName() + "'");
			}
			
			context.getMappingStrategy().configFieldMappingByDomain(emb, fmb, domain);
			
			return true;
		}else if(null != domainAnnotation) {
		    FieldDomain domain = domains.getOrCreateFieldDomain(domainAnnotation.annotationType(), adomain);
		    context.getMappingStrategy().configFieldMappingByDomain(emb, fmb, domain);
		    return true;
		}
		return false;
	}
	
	protected boolean mappingFieldColumnByMetaName(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb,MetaName a){
		String metaFieldName = null;
		
		if(null != a){
			metaFieldName = Strings.firstNotEmpty(a.value(),a.reserved().getFieldName());
		}else{
			Annotation metaNameAnnotation = Classes.getAnnotationByMetaType(fmb.getBeanProperty().getAnnotations(),MetaName.class);
			if(null != metaNameAnnotation){
				MetaName metaDomain = metaNameAnnotation.annotationType().getAnnotation(MetaName.class);
				metaFieldName = Strings.firstNotEmpty(metaDomain.value(),metaDomain.reserved().getFieldName());
			}
		}
		
		if(!Strings.isEmpty(metaFieldName)) {
			fmb.setMetaFieldName(metaFieldName);
			fmb.setReservedMetaFieldName(ReservedMetaFieldName.tryForName(metaFieldName));
			return true;
		}
		
		return false;
	}
	
	protected void mappingFieldColumnByAnnotation(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb,Id a){
		if(null != a){
			emb.setIdDeclared(true);
			
			fmb.getColumn().setPrimaryKey(true);
			
			if(a.generate()) {
				String generatorName = a.generator();
				if(!Strings.isEmpty(generatorName)){
					fmb.setIdGenerator(beanFactory.getBean(IdGenerator.class,generatorName));
				}
			}
			
		}
	}
	
	protected void mappingFieldColumnByAnnotation(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb,NotEmpty a){
		if(null != a && null == fmb.getNullable()){
			fmb.setNullable(false);
		}
	}
	
	protected void mappingFieldColumnByAnnotation(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb,NotNull a){
		if(null != a && null == fmb.getNullable()){
			fmb.setNullable(false);
		}
	}
	
	protected void mappingFieldColumnByAnnotation(MetadataContext context,EntityMappingBuilder emb,FieldMappingBuilder fmb,Required a){
		if(null != a && null == fmb.getNullable()){
			fmb.setNullable(false);
		}
	}
	
	protected void mappingManyToOneByClassAnnotation(MetadataContext context,EntityMappingBuilder emb,ManyToOne[] annotations) {
		for(ManyToOne a : annotations){
			if(a.targetEntityType().equals(void.class)){
				throw new MappingConfigException("The 'ManyToOne' annotation at class '" + emb.getEntityClass() + "' must defines 'targetEntityType'");
			}
			
			RelationMappingBuilder rmb = new RelationMappingBuilder();
			
			rmb.setType(RelationType.MANY_TO_ONE);
			rmb.setName(a.name());
			rmb.setOptional(a.optional().getValue());
			rmb.setTargetEntityType(a.targetEntityType());
			
			//join fields
			createManyToOneJoinFields(emb, rmb, a.fields());
			
			emb.addRelationMapping(rmb);
		}
	}
	
	protected void mappingManyToManyByClassAnnotation(MetadataContext context,EntityMappingBuilder emb,ManyToMany[] annotations) {
		for(ManyToMany a : annotations) {
			if(a.targetEntityType().equals(void.class)){
				throw new MappingConfigException("The 'ManyToMany' annotation at class '" + emb.getEntityClass() + "' must defines 'targetEntityType'");
			}
			
			RelationMappingBuilder rmb = new RelationMappingBuilder();
			
			rmb.setType(RelationType.MANY_TO_MANY);
			rmb.setName(a.name());
			rmb.setTargetEntityType(a.targetEntityType());
			
			if(!a.joinEntityType().equals(void.class)){
				rmb.setJoinEntityType(a.joinEntityType());
			}
			
			rmb.setJoinTableName(a.joinTableName());
			
			emb.addRelationMapping(rmb);
		}
	}
	
	protected boolean mappingManyToOneByPropertyAnnotation(MetadataContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb, BeanProperty bp, ManyToOne a) {
		if(null == a){
			return false;
		}
		
		rmb.setName(a.name());
		rmb.setType(RelationType.MANY_TO_ONE);
		rmb.setOptional(a.optional().getValue());
		
		if(a.targetEntityType().equals(void.class)){
			rmb.setTargetEntityType(bp.getType());	
		}else{
			rmb.setTargetEntityType(a.targetEntityType());
		}

		JoinField[] jfs = bp.getField().getDeclaredAnnotationsByType(JoinField.class);
		createManyToOneJoinFields(emb, rmb, jfs);
		
		return true;
	}
	
	protected boolean mappingManyToManyByPropertyAnnotation(MetadataContext context,EntityMappingBuilder emb,RelationMappingBuilder rmb, BeanProperty bp, ManyToMany a) {
		if(null == a){
			return false;
		}
		
		rmb.setName(a.name());
		rmb.setType(RelationType.MANY_TO_MANY);
		
		if(a.targetEntityType().equals(void.class)){
			rmb.setTargetEntityType(bp.getType());	
		}else{
			rmb.setTargetEntityType(a.targetEntityType());
		}
		
		if(!a.joinEntityType().equals(void.class)){
			rmb.setJoinEntityType(a.joinEntityType());
		}
		
		rmb.setJoinTableName(a.joinTableName());
		
		return true;
	}
	
	protected void createManyToOneJoinFields(EntityMappingBuilder emb,RelationMappingBuilder rmb, JoinField[] jfs) {
		if(jfs.length > 0){
			for(JoinField jf : jfs){
				JoinFieldMappingBuilder jfmb = new JoinFieldMappingBuilder();
				
				jfmb.setLocalFieldName(jf.name());
				jfmb.setLocalColumnName(jf.column());
				jfmb.setReferencedFieldName(jf.referencedFieldName());

				rmb.addJoinField(jfmb);
			}
		}
	}
}