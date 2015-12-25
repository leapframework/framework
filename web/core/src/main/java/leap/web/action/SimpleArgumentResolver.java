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
package leap.web.action;

import javax.servlet.http.Part;

import leap.lang.Classes;
import leap.lang.convert.Converts;
import leap.web.App;

/**
 * An {@link ArgumentResolver} for resolving simple argument type.
 * 
 * @see TypeStrategy#isSimpleType(Class, java.lang.reflect.Type)
 */
public class SimpleArgumentResolver extends AbstractArgumentResolver {
	
	public SimpleArgumentResolver(App app,Action action,Argument argument) {
	    super(app,action,argument);
    }

	@Override
    public Object resolveValue(ActionContext ac, Argument arg) throws Throwable {
		Object value = getParameter(ac, arg);
		if(null == value){
			return Classes.getDefaultValue(arg.getType());
		}else{
			//TODO : hard code part.
			if(value instanceof Part) {
				return convertFromPart((Part)value, arg);
			}
			return Converts.convert(value, arg.getType(), arg.getGenericType());
		}
    }
}