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
package leap.orm;

import leap.core.AppContext;
import leap.core.exception.DataAccessException;
import leap.lang.Strings;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;

public class Orm {

	public static final String DEFAULT_NAME = OrmConstants.DEFAULT_NAME;
	
	public static Dao dao() throws DataAccessException {
		return getDao(DEFAULT_NAME,true);
	}
	
	public static Dao dao(String name){
		return getDao(name,Strings.equals(DEFAULT_NAME, name));
	}

    public static Dao dao(Class<?> entityClass) {
        return dao(context(entityClass).getName());
    }

	public static Dmo dmo() throws DataAccessException {
		return getDmo(DEFAULT_NAME,true);
	}
	
	public static Dmo dmo(String name){
		return getDmo(name,Strings.equals(DEFAULT_NAME, name));
	}

    public static OrmContext context(Class<?> entityClass) {
        for(OrmContext context : AppContext.factory().getBeans(OrmContext.class)) {
            if(context.getMetadata().tryGetEntityMapping(entityClass) != null) {
                return context;
            }
        }
        throw new IllegalStateException("Orm context not found for entity class '" + entityClass + "'");
    }

    public static boolean hasContexts() {
        return !AppContext.factory().getBeans(OrmContext.class).isEmpty();
    }
	
	public static OrmContext context() {
		return context(DEFAULT_NAME);
	}
	
	public static OrmContext context(String name) {
		return getContext(name, Strings.equals(DEFAULT_NAME, name));
	}
	
	public static OrmMetadata metadata(){
		return context(DEFAULT_NAME).getMetadata();
	}
	
	public static OrmMetadata metadata(String name){
		return context(name).getMetadata();
	}
	
	protected static OrmContext getContext(String name,boolean primary){
		return primary ? AppContext.factory().getBean(OrmContext.class) : AppContext.factory().getBean(OrmContext.class,name);
	}
	
	protected static Dao getDao(String name,boolean primary){
		return primary ? AppContext.factory().getBean(Dao.class) : AppContext.factory().getBean(Dao.class,name);
	}
	
	protected static Dmo getDmo(String name,boolean primary){
		return primary ? AppContext.factory().getBean(Dmo.class) : AppContext.factory().getBean(Dmo.class,name);
	}
	
	protected Orm(){
		
	}
}