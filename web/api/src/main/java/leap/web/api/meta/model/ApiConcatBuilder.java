/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta.model;


public class ApiConcatBuilder extends ApiObjectBuilder<ApiConcat> {
	
	protected String name;
	protected String url;
	protected String email;

	public ApiConcatBuilder() {
		
	}
	
	public String getName() {
		return name;
	}

	public ApiConcatBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public ApiConcatBuilder setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public ApiConcatBuilder setEmail(String email) {
		this.email = email;
		return this;
	}

	@Override
    public ApiConcat build() {
	    return new ApiConcat(name, url, email, attrs);
    }

}
