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

import leap.core.validation.annotations.Required;
import leap.web.annotation.AcceptValidationError;
import leap.web.annotation.Consumes;
import leap.web.annotation.Produces;
import leap.web.annotation.Validate;

public class ValidationTestController {
	
	public String required(@Required String v) {
		return v;
	}
	
	@AcceptValidationError
	public String required1(@Required String v) {
	    return "OK";
	}

    @Produces("json")
    @Consumes("json")
    public void validate(@Validate VBean bean) {

    }

    public static final class VBean {

        public @Required String name;

    }
}