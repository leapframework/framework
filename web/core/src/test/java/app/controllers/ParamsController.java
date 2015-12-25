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
package app.controllers;

import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.web.Params;
import leap.web.action.ControllerBase;
import leap.web.annotation.QueryParam;

public class ParamsController extends ControllerBase {
	
	public String get(Params params) {
		return params.get("s");
	}
	
	public String getArray(Params params) {
		return Strings.join(params.getArray("s"),",");
	}
	
	public String getInt(Params params) {
		return String.valueOf(params.getInteger("i"));
	}
	
	public String getIntArray(Params params) {
		return Converts.toString(params.getIntArray("i"));
	}

	public String queryParam(@QueryParam String s) {
	    return s;
	}
}