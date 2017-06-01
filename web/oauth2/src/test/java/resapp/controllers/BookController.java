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
package resapp.controllers;

import leap.web.annotation.Path;
import leap.web.annotation.http.GET;
import tested.models.Book;

public class BookController {

	@GET
	@Path("")
	public Book get(String id) {
		if("1".equals(id)) {
			return new Book(id, "Test Book");
		}
		return null;
	}
}