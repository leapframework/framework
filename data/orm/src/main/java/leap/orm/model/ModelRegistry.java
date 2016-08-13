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

import leap.core.AppConfigException;
import leap.lang.annotation.Internal;
import leap.lang.beans.BeanType;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.exception.ObjectNotFoundException;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.validation.FieldValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Internal
public class ModelRegistry {
	private static final ThreadLocal<OrmContext>       localOrmContext = new ThreadLocal<>();
	private static final Map<String, ModelInfo>        modelInfos	   = new HashMap<>();
	private static final Map<String, ModelContext>     modelContexts   = new HashMap<>();
	
	public static OrmContext getThreadLocalContext(){
		return localOrmContext.get();
	}

	public static void setThreadLocalCotnext(OrmContext context){
		localOrmContext.set(context);
	}
	
	public static void removeThreadLocalContext(){
		localOrmContext.remove();
	}
	
	public static ModelInfo removeModelInfo(String className){
		return modelInfos.remove(className);
	}
	
	static ModelInfo getOrCreateModelInfo(String className){
		ModelInfo mi = modelInfos.get(className);
		if(null == mi){
			mi = new ModelInfo();
			modelInfos.put(className, mi);
		}
		return mi;
	}
	
	public static ModelContext getModelContext(String className){
		ModelContext mc = modelContexts.get(className);
		
		if(null == mc){

            if(!Orm.hasContexts()) {
                throw new AppConfigException("DataSource(s) must be configured!");
            }else{
                throw new ObjectNotFoundException("Model '" + className + "' not found in registry");
            }

		}
		
		return mc;
	}
	
	public static ModelContext tryGetModelContext(String className){
		return modelContexts.get(className);
	}
	
	public static void addModelContext(ModelContext modelContext){
		modelContexts.put(modelContext.getModelClass().getName(), modelContext);
	}
	
	public static final class ModelInfo {
		
		private final Map<String, FieldInfo> fields = new SimpleCaseInsensitiveMap<ModelRegistry.FieldInfo>();
		
		ModelInfo() {
        }
		
		public Map<String, FieldInfo> fields(){
			return fields;
		}
		
		public FieldInfo getField(String field){
			return fields.get(field);
		}
		
		public FieldInfo getOrCreateField(String field){
			FieldInfo fi = fields.get(field);
			if(null == fi){
				fi = new FieldInfo();
				fields.put(field, fi);
			}
			return fi;
		}
	}
	
	public static final class FieldInfo {
		private final List<FieldValidator> validators = new ArrayList<FieldValidator>();
		
		FieldInfo() {
        }

		public List<FieldValidator> validators() {
			return validators;
		}
		
		public FieldInfo addValidator(FieldValidator validator){
			validators.add(validator);
			return this;
		}
	}
	
	public static final class ModelContext {
		private final OrmContext    		   ormContext;
		private final EntityMapping 		   entityMapping;
		private final Dao					   dao;
		private final Dmo					   dmo;
		private final BeanType      		   beanType;
		private final Map<String, ModelFinder> finders = new HashMap<String, ModelFinder>();
		
		public ModelContext(OrmContext ormContext,EntityMapping em,Dao dao,Dmo dmo){
			this.ormContext    = ormContext;
			this.entityMapping = em;
			this.dao           = dao;
			this.dmo           = dmo;
			this.beanType	   = BeanType.of(em.getModelClass());
		}

		public Class<? extends Model> getModelClass() {
			return entityMapping.getModelClass();
		}

		public BeanType getBeanType(){
			return beanType;
		}
		
		public OrmContext getOrmContext() {
			OrmContext tlOrmContext = getThreadLocalContext();
			if(null != tlOrmContext){
				return tlOrmContext;
			}
			return ormContext;
		}

		public EntityMapping getEntityMapping() {
			OrmContext tlOrmContext = getThreadLocalContext();
			if(null != tlOrmContext){
				if(tlOrmContext == ormContext){
					return entityMapping;
				}else{
					return tlOrmContext.getMetadata().getEntityMapping(entityMapping.getEntityName());
				}
			}
			return entityMapping;
		}

		public Dao getDao() {
			OrmContext tlOrmContext = getThreadLocalContext();
			
			if(null != tlOrmContext){
				return tlOrmContext.getAppContext().getBeanFactory().getBean(Dao.class,tlOrmContext.getName());
			}
			
			return dao;
		}

		public Dmo getDmo() {
			OrmContext tlOrmContext = getThreadLocalContext();
			
			if(null != tlOrmContext){
				return tlOrmContext.getAppContext().getBeanFactory().getBean(Dmo.class,tlOrmContext.getName());
			}
			
			return dmo;
		}
		
		public ModelFinder getFinder(String name) throws ObjectNotFoundException {
			ModelFinder finder = finders.get(name);
			if(null == finder){
				throw new ObjectNotFoundException("Finder '" + name + "' not exists in Model '" + entityMapping.getModelClass().getName() + "'");
			}
			return finder;
		}
		
		protected void addFinder(String name,ModelFinder finder){
			finders.put(name, finder);
		}
	}
	
	private ModelRegistry(){
		
	}
}
