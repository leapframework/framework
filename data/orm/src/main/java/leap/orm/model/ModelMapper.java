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

import leap.core.AppContext;
import leap.core.AppContextAware;
import leap.orm.OrmContext;
import leap.orm.OrmContextInitializable;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.Mapper;
import leap.orm.mapping.MappingConfigContext;
import leap.orm.metadata.MetadataException;
import leap.orm.model.ModelRegistry.ModelContext;

public class ModelMapper implements Mapper,AppContextAware,OrmContextInitializable {
	
	protected AppContext appContext;
	
	@Override
    public void setAppContext(AppContext appContext) {
		this.appContext = appContext;
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

	@Override
	public void loadMappings(final MappingConfigContext context) throws MetadataException {

	}
}