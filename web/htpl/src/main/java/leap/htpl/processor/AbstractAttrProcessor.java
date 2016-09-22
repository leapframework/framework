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
package leap.htpl.processor;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.lang.Ordered;
import leap.lang.OrderedBase;

public abstract class AbstractAttrProcessor extends OrderedBase implements AttrProcessor {
	
	@Override
    public boolean required() {
	    return true;
    }

	@Override
    public Node processEndElement(HtplEngine engine, HtplDocument doc, Element e, Attr a) throws Throwable {
	    return e;
    }

	protected void acceptAttrWithProcessor(Element e,Attr a){
		//<a href="aaa" ht-href="bbb"/> -> <a href="bbb"/>
		
		//removes the attribute with same local name of the attribute
		e.removeAttribute(null, a.getLocalName());
		
		//removes the prefix and processor in attribute
		a.setPrefix(null);
		a.setProcessor(null);
	}
}