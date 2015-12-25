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
package app.controllers;

import leap.web.Results;

/**
 * Test servlet 3.0 features
 */
public class Servlet3Controller {

	/**
	 * META-INF/resources/test_resource.jsp
	 */
	public void testResource(){
		Results.forward("/test_resource.jsp");
	}

	/**
	 * META-INF/resoruces/WEB-INF/views/servlet3/test_resource1.jsp
	 */
	public void testResource1(){

	}
	
	public void testWebjarResource(){
		Results.forward("/webjars/bootstrap/2.3.0/css/bootstrap.min.css");
	}
	
}
