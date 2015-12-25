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
package leap.htpl.processor.core;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.htpl.processor.AbstractNamedAttrProcessor;
import leap.lang.Strings;
import leap.lang.accessor.AttributeAccessor;

public class ModelAttrProcessor extends AbstractNamedAttrProcessor {
	
	public static final String ATTR_NAME 			= "model";
	public static final String MODEL_NAME_ATTRIBUTE = "MODEL_NAME";
	public static final String DEFAULT_MODEL_NAME   = "model";
	
	public static void setModelName(AttributeAccessor context,String modelName){
		context.setAttribute(MODEL_NAME_ATTRIBUTE, modelName);
	}
	
	public static void removeModelName(AttributeAccessor context){
		context.removeAttribute(MODEL_NAME_ATTRIBUTE);
	}
	
	/**
	 * Returns the model name was setted in the given context.
	 * 
	 * <p>
	 * Returns {@link #DEFAULT_MODEL_NAME} if the model name not exists in the given context.
	 */
	public static String getModelName(AttributeAccessor context){
		String modelName = (String)context.getAttribute(MODEL_NAME_ATTRIBUTE);
		return Strings.isEmpty(modelName) ? DEFAULT_MODEL_NAME : modelName;
 	}

	public ModelAttrProcessor() {
		super(ATTR_NAME);
	}

	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		//Sets the model name in the compile context.
		setModelName(doc, attr.getString());
		return e;
	}

	@Override
    public Node processEndElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		//Removes the model name from the compile context.
		removeModelName(doc);
		return e;
	}
}