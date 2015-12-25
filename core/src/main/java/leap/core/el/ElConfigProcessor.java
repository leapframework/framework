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
package leap.core.el;

import leap.core.AppConfigContext;
import leap.core.AppConfigException;
import leap.core.AppConfigProcessor;
import leap.core.el.ElConfigFunctions.ElConfigFunction;
import leap.lang.xml.XmlReader;

public class ElConfigProcessor implements AppConfigProcessor {
	
	public static final String NAMESPACE_URI = "http://www.leapframework.org/schema/el/config";

	private static final String FUNCTIONS_ELEMENT = "functions";
	private static final String FUNCTION_ELEMENT  = "function";
	private static final String CLASS_ATTRIBUTE   = "class";
	private static final String PREFIX_ATTRIBUTE  = "prefix";
	private static final String NAME_ATTRIBUTE    = "name";
	private static final String METHOD_ATTRIBUTE  = "method";
	
	@Override
    public String getNamespaceURI() {
	    return NAMESPACE_URI;
    }

	@Override
	public void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException {
		ElConfigFunctions funcs = context.getOrCreateExtension(ElConfigFunctions.class);
		
		if(reader.isStartElement(FUNCTIONS_ELEMENT)){
			readFunctions(context, reader, funcs);
		}else{
			throw new AppConfigException("Unsupported el config element '" + reader.getElementLocalName() 
									     + "' in xml '" + reader.getSource() + "'");
		}
	}
	
	protected void readFunctions(AppConfigContext context, XmlReader reader, ElConfigFunctions funcs) {
		String prefix	 = reader.getAttribute(PREFIX_ATTRIBUTE);
		String className = reader.getAttributeRequired(CLASS_ATTRIBUTE);
		
		while(true){
			reader.next();
			
			if(reader.isStartElement()){
				if(reader.isStartElement(FUNCTION_ELEMENT)){
					readFunction(context, reader, funcs, prefix, className);
				}else{
					throw new AppConfigException("Unsupported el config element '" + reader.getElementName() + 
												 "' in xml '" + reader.getSource() + "'");
				}
			}else if(reader.isEndElement(FUNCTIONS_ELEMENT)){
				break;
			}
		}
	}
	
	protected void readFunction(AppConfigContext context, XmlReader reader, ElConfigFunctions funcs, String prefix, String className) {
		String methodDesc = reader.getAttributeRequired(METHOD_ATTRIBUTE);
		String funcName   = reader.getAttribute(NAME_ATTRIBUTE);
		
		/*
		Method m = null;
		try {
	        m = Reflection.getMethodByNameOrDesc(cls, methodDesc);
        } catch (Exception e) {
        	throw new AppConfigException("Invalid method name '" + methodDesc + "' in xml '" + reader.getSource() + "'", e);
        }
		
		if(Strings.isEmpty(funcName)){
			funcName = m.getName();
		}
		
		if(!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())){
			throw new AppConfigException("Function method '" + methodDesc + 
										 "' must be 'public static', check xml '" + reader.getSource() + "'");
		}
		 */		
		ElConfigFunction func = new ElConfigFunction();
		func.source		= reader.getSource();
		func.funcPrefix = prefix;
		func.funcName   = funcName;
		func.className  = className;
		func.methodDesc = methodDesc;
		
		funcs.add(func);
		
		reader.nextToEndElement(FUNCTION_ELEMENT);
	}
}