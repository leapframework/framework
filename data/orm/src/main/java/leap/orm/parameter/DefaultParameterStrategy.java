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
package leap.orm.parameter;

import java.security.InvalidParameterException;
import java.util.Map;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.params.ParamsFactory;
import leap.lang.Beans;
import leap.lang.convert.Converts;
import leap.lang.exception.InvalidParametersException;
import leap.lang.params.ArrayParams;
import leap.lang.params.BeanParams;
import leap.lang.params.MapParams;
import leap.lang.params.Params;
import leap.lang.params.UnsupportedParametersException;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;

public class DefaultParameterStrategy implements ParameterStrategy {
	
	protected @Inject @M ParamsFactory factory;
	
    public void setFactory(ParamsFactory factory) {
		this.factory = factory;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, Object> toMap(Object object) throws InvalidParametersException {
    	if(null == object){
    		throw new InvalidParameterException("Cannot convert null to map");
    	}
		if(object instanceof Map){
			return ((Map)object);
		}else if(object instanceof Params){
			return ((Params)object).map();
		}
		
		if(Beans.isSimpleProperty(object.getClass())){
			throw new InvalidParameterException("Cannot convert value of simple type '" + object.getClass().getName() + "' to map");
		}
		
		return Beans.toMap(object);
    }

	@Override
    @SuppressWarnings({"rawtypes","unchecked"})
    public Params createIdParameters(OrmContext context, EntityMapping em, Object id) throws InvalidParametersException {
		if(null == id){
			throw new InvalidParametersException("The id value must not be null");
		}
		
		String[] keys = em.getKeyFieldNames();
		
		if(id.getClass().isArray()){
			Object[] array = Converts.toObjectArray(id);
			
			if(array.length != keys.length){
				throw new InvalidParameterException("The passed in array's length " + array.length + " not equals to keys size " + keys.length);
			}
			
			return new ArrayParams(Converts.toObjectArray(id));
		}else if(id instanceof Map){
			return new MapParams((Map)id);
		}else {
			if(null != em.getEntityClass() && em.getEntityClass().isAssignableFrom(id.getClass())){
				return new BeanParams(id);
			}else if(keys.length == 1){
				return new ArrayParams(id);
			}else{
				throw new UnsupportedParametersException("The class '" + id.getClass().getName() + "' not supported as id parameter");
			}
		}
    }

	@Override
    public Params createParams(Object param) {
	    return factory.createParams(param);
    }

	@Override
    public Params createParams(Object... params) {
	    return factory.createParams(params);
    }
}