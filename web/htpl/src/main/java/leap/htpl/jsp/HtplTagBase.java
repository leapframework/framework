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
package leap.htpl.jsp;

import javax.servlet.jsp.tagext.TagSupport;

import leap.core.AppContext;
import leap.htpl.HtplEngine;

public abstract class HtplTagBase extends TagSupport {

	private static final long serialVersionUID = 7869740867319444029L;

	private static HtplEngine engine;
	
	protected final HtplEngine engine() {
	    if(null == engine){
	    	engine = AppContext.current().getBeanFactory().getBean(HtplEngine.class);
	    }
	    return engine;
	}

}