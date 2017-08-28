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
package leap.orm.model;

import java.lang.reflect.Modifier;

import leap.core.AppContext;
import leap.core.AppContextAware;
import leap.lang.Strings;
import leap.lang.reflect.ReflectMethod;
import leap.orm.OrmContext;
import leap.orm.OrmContextInitializable;
import leap.orm.annotation.Entity;
import leap.orm.annotation.EntityClass;
import leap.orm.annotation.Finder;
import leap.orm.annotation.NonEntity;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.Mapper;
import leap.orm.mapping.MappingConfigContext;
import leap.orm.metadata.MetadataException;
import leap.orm.model.ModelRegistry.ModelContext;

public class ModelMapper implements Mapper,AppContextAware,OrmContextInitializable {
	
	private static final String MODEL_NAME_SUFFIX = "Model";
	
	protected AppContext appContext;
	
	@Override
    public void setAppContext(AppContext appContext) {
		this.appContext = appContext;
    }
	
	@Override
    public void postInitialize(OrmContext context) throws Exception {
		Dao dao = appContext.getBeanFactory().getBean(Dao.class,context.getName());
		Dmo dmo = appContext.getBeanFactory().getBean(Dmo.class,context.getName());
		
		for(EntityMapping em : context.getMetadata().getEntityMappingSnapshotList()){
			Class<? extends Model> cls = em.getModelClass();

			if(null != cls){
				ModelContext modelContext = ModelRegistry.tryGetModelContext(cls.getName());
				
				//TODO : Duplicate orm context in same model
				
				if(null == modelContext){
					registerModel(context, em, dao, dmo);
				}
			}
		}
    }
	
	protected void registerModel(OrmContext context, EntityMapping em, Dao dao, Dmo dmo){
		
		ModelContext modelContext = new ModelContext(context, em, dao, dmo);

		for(ReflectMethod method : modelContext.getBeanType().getReflectClass().getMethods()){
			if(method.isAnnotationPresent(Finder.class)){
				modelContext.addFinder(method.getName(), new ModelFinder(modelContext, method));
			}
		}
		
		ModelRegistry.addModelContext(modelContext);
	}

	@Override
	public void loadMappings(final MappingConfigContext context) throws MetadataException {
		//ResourceSet resources = appContext.getConfig().getResources();
		
		//final OrmModelsConfigs mcs = appContext.getConfig().getExtension(OrmModelsConfigs.class);
		//final String		   ds  = context.getOrmContext().getName();
		//OrmModelsConfig  models  = null == mcs ? null : mcs.getModelsConfig(ctxName);
		/*
		resources.processClasses((cls) -> {
			if(isModelClass(cls)){
				
				if(cls.getSimpleName().equals("Ds1Model")) {
					System.out.println(ds);
				}
				
				for(Entry<String, OrmModelsConfig> entry : mcs.getModelsConfigMap().entrySet()) {
					String name = entry.getKey();
					OrmModelsConfig models = entry.getValue();
					
					if(name.equalsIgnoreCase(ds)) {
						if(!models.isModel(cls)) {
							return;
						}else{
							break;
						}
					}else{
						if(models.isModel(cls)) {
							return;
						}
					}
				}
				
				loadModelClass(context,(Class<Model>) cls);
			}
		});
		*/
		
	}
	
	protected boolean isModelClass(Class<?> cls){
		return !cls.isAnnotationPresent(NonEntity.class) &&
			   !Modifier.isAbstract(cls.getModifiers()) && Model.class.isAssignableFrom(cls);
	}
	
	protected void loadModelClass(MappingConfigContext context,Class<? extends Model> cls) {
		//resovle entity mapping.
		String entityName = null;
		EntityMappingBuilder emb = null;
		
		Entity a = cls.getAnnotation(Entity.class);
		if(null != a){
			entityName = a.name();
			if(Strings.isEmpty(entityName)){
				entityName = a.value();
			}
		}

		if(Strings.isEmpty(entityName)){
			entityName = resolvePossibleEntityName(context, cls);	
		}
		
		emb = resolveEntityMapping(context, entityName);
		
		if(null == emb){
			Class<?> entityClass = resolveEntityClass(context, cls);
			
			if(null != entityClass){
				emb = context.getEntityMapping(entityClass);
			}
		}
		
		if(null == emb){
			emb = context.getMappingStrategy().createEntityClassMapping(context, cls);
			context.addEntityMapping(emb);
		}
		emb.setModelClass(cls);
	}
	
	protected String resolvePossibleEntityName(MappingConfigContext context, Class<? extends Model> cls){
		String entityName = cls.getSimpleName();
			
		if(entityName.endsWith(MODEL_NAME_SUFFIX)){
			return entityName.substring(0,entityName.length() - MODEL_NAME_SUFFIX.length());
		}
		
		return entityName;
	}
	
	protected Class<?> resolveEntityClass(MappingConfigContext context, Class<? extends Model> cls){
		EntityClass a = cls.getAnnotation(EntityClass.class);

		if(null != a){
			return a.value();
		}
		
		return null;
	}
	
	protected EntityMappingBuilder resolveEntityMapping(MappingConfigContext context, String modelName){
		modelName = modelName.toLowerCase();
		
		for(EntityMappingBuilder emb : context.getEntityMappings()){
			String entityName = emb.getEntityName().toLowerCase();
			
			if(modelName.equals(entityName) || 
			   (context.getNamingStrategy().isPluralOf(modelName, entityName)) || 
			   (context.getNamingStrategy().isPluralOf(entityName, modelName))){
				
				return emb;
			}
		}
		
		return null;
	}
}