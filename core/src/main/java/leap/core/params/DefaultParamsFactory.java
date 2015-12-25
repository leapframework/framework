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
package leap.core.params;

import java.util.Map;

import leap.lang.Beans;
import leap.lang.params.BeanParams;
import leap.lang.params.CompositeParams;
import leap.lang.params.EmptyParams;
import leap.lang.params.MapParams;
import leap.lang.params.Params;
import leap.lang.params.ParamsMap;
import leap.lang.params.ParamsWrappable;
import leap.lang.params.UnsupportedParametersException;

public class DefaultParamsFactory implements ParamsFactory {
	
	public static final DefaultParamsFactory INSTANCE = new DefaultParamsFactory();
	
	@SuppressWarnings("unchecked")
    public Params createParams(Object param) throws IllegalStateException {
		if(null == param){
			return new ParamsMap();
		}
		
		if(param instanceof Params){
			return (Params)param;
		}
		
		if(param instanceof ParamsWrappable){
			return ((ParamsWrappable) param).params();
		}
		
		if(param instanceof Map){
			return new MapParams((Map<String,Object>)param);
		}
		
		if(isBeanParameters(param.getClass())){
			return new BeanParams(param);
		}
		
		throw new UnsupportedParametersException("unsupported parameters type '" + param.getClass().getName() + "'");
	}

	public Params createParams(Object... params){
		if(null == params || params.length == 0){
			return EmptyParams.INSTANCE;
		}
		
		CompositeParams compositeParameters = new CompositeParams(); 
		
		for(int i=0;i<params.length;i++){
			Object param = params[i];
			
			if(null != param){
				compositeParameters.add(createParams(param));
			}
		}
		
		return compositeParameters;
	}
	
	protected static boolean isBeanParameters(Class<?> cls){
	    if(Iterable.class.isAssignableFrom(cls)){
	    	return false;
	    }
	    
	    if(Map.class.isAssignableFrom(cls)){
	    	return false;
	    }
	    
	    if(Beans.isSimpleProperty(cls)){
	    	return false;
	    }
	    
	    if(cls.equals(Object.class)){
	    	return false;
	    }
	    
	    return true;
	}
	
	protected DefaultParamsFactory(){
		
	}
}