/*
 * Copyright 2015 the original author or authors.
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

import leap.web.annotation.Path;
import leap.web.annotation.http.POST;
import leap.web.annotation.http.PUT;

public class VerbTestController {

	@PUT
	@Path("t")
	public String testPut() {
		return "put";
	}
	
	@POST
	@Path("t")
	public String testPost() {
		return "post";
	}
	
	@Path("t")
	public String test() {
		return "test";
	}
	
}
