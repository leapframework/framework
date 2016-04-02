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
package leap.htpl.interceptor;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;

public abstract class ElementProcessInterceptor implements ProcessInterceptor {

	@Override
    public final void preProcessNode(HtplEngine engine, HtplDocument document, Node node) throws Throwable {
		if(node instanceof Element) {
            preProcessElement(engine, document, (Element)node);
		}
    }

	@Override
    public final Node postProcessNode(HtplEngine engine, HtplDocument document, Node node, Node result) throws Throwable {
		if(node instanceof Element) {
			return postProcessElement(engine, document, (Element)node, result);
		}
        return result;
    }

	protected void preProcessElement(HtplEngine engine, HtplDocument document, Element e) throws Throwable {

	}
	
	protected Node postProcessElement(HtplEngine engine, HtplDocument document, Element e, Node result) throws Throwable {
		return result;
	}

}