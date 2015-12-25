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
import leap.htpl.ast.RenderFragment;
import leap.htpl.processor.AbstractAttrProcessor;

public class RenderAttrProcessor extends AbstractAttrProcessor {
	
	public static final String ATTR_NAME = "render";

	public static final String RENDER_FRAGMENT_ATTR = "render-fragment";
	
	public RenderAttrProcessor() {

	}

	@Override
    public boolean supports(Element e, Attr a) {
	    return a.getLocalName().equalsIgnoreCase(RENDER_FRAGMENT_ATTR);
    }

	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		String name = attr.getString();
		e.removeAttribute(attr);
		e.setChildNode(new RenderFragment(name));
		return e;
	}

}