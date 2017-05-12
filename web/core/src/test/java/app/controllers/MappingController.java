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

import leap.web.annotation.RequestMapping;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;

public class MappingController {
	
	public void index(){
		
	}

//	@RequestMapping(path="param_action",params="action=action1")
//	public String paramAction1(String action){
//		return "action1";
//	}
//
//	@RequestMapping(path="param_action",params="action=action2")
//	public String paramAction2(String action){
//		return "action2";
//	}
//
	public String paramAction(){
		return "otherAction";
	}

	@GET
	public String paramAction0() {
		return "get";
	}
	
	@POST
	@RequestMapping(params="do=1")
	public String paramAction0(int i) {
		return "post";
	}
	
	@GET
	public String upload() {
		return "get";
	}
	
	@POST
	@RequestMapping(params="do=1")
	public String upload(int i) {
		return "post";
	}
	
}
