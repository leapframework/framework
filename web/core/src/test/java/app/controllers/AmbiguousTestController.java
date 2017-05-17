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

import leap.web.Request;
import leap.web.action.ControllerBase;
import leap.web.annotation.http.GET;

public class AmbiguousTestController extends ControllerBase {

	@GET("/ambiguous/case1/user/in{id}")
	public String in_() {
		return "in_";
	}
	@GET("/ambiguous/case1/user/{id}")
	public String _1() {
		return "_";
	}


	@GET("/ambiguous/case2/user/in{id}")
	public String in_2() {
		return "in_";
	}
	@GET("/ambiguous/case2/user/{id}in")
	public String _in() {
		return "_in";
	}


	@GET("/ambiguous/case3/user/in{id}")
	public String in_3() {
		return "in_";
	}
	@GET("/ambiguous/case3/user/{id}ing")
	public String _ing() {
		return "_ing";
	}


	@GET("/ambiguous/case4/user/{id}")
	public String _() {
		return "_";
	}
	@GET("/ambiguous/case4/user/{name}")
	public String name() {
		return "_";
	}


	@GET("/ambiguous/case5/user/{id}")
	public String _5() {
		return "_";
	}
	@GET("/ambiguous/case5/{user}/id")
	public String id() {
		return "id";
	}


	@GET("/ambiguous/case6/user/info")
	public String info() {
		return "info";
	}
	@GET("/ambiguous/case6/user/{id}")
	public String info6() {
		return "_";
	}
}