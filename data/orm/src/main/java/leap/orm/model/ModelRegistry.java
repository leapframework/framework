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
import leap.orm.OrmContextInitializable;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.validation.FieldValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Internal
public class ModelRegistry implements OrmContextInitializable {
	private static final ThreadLocal<OrmContext>       localOrmContext = new ThreadLocal<>();
	private static final Map<String, ModelContext>     modelContexts   = new HashMap<>();
	
	public static OrmContext getThreadLocalContext(){
		return localOrmContext.get();
	}

	public static void setThreadLocalContext(OrmContext context){
		localOrmContext.set(context);
	}
	
	public static void removeThreadLocalContext(){
		localOrmContext.remove();
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
	
	private static void addModelContext(ModelContext modelContext){
		modelContexts.put(modelContext.getModelClass().getName(), modelContext);
        if(null != modelContext.getExtendModelClass()) {
            modelContexts.put(modelContext.getExtendModelClass().getName(), modelContext);
        }
	}

    @Override
    public void postInitialize(OrmContext context) throws Exception {
        Dao dao = context.getDao();
        Dmo dmo = context.getDmo();

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

        ModelRegistry.addModelContext(modelContext);
    }

    public static final class ModelContext {
		private final OrmContext    		   ormContext;
		private final EntityMapping 		   entityMapping;
		private final Dao					   dao;
		private final Dmo					   dmo;
		private final BeanType      		   beanType;

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

        public Class<? extends Model> getExtendModelClass() {
            if(null != entityMapping.getExtendedEntityClass() && Model.class.isAssignableFrom(entityMapping.getExtendedEntityClass())) {
                return (Class<Model>)entityMapping.getExtendedEntityClass();
            }
            return null;
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
	}
	
	private ModelRegistry(){
		
	}
}
