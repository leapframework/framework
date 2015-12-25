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
package leap.web.action;

import java.util.HashMap;
import java.util.Map;

public class ActionMapping {

	protected String 			  path;
	protected String 			  method;
	protected Map<String, String> params = new HashMap<String, String>();
	
	public ActionMapping() {
	    super();
    }
	
	public ActionMapping(String path) {
		this(path,"*");
	}
	
	public ActionMapping(String path,String method) {
		this.path   = path;
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParam(String name,String value) {
		params.put(name, value);
	}
}