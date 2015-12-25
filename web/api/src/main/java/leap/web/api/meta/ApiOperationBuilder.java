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
package leap.web.api.meta;

import java.util.ArrayList;
import java.util.List;

import leap.lang.Arrays2;
import leap.lang.Builders;
import leap.lang.http.HTTP;

public class ApiOperationBuilder extends ApiNamedWithDescBuilder<ApiOperation> {
	
	protected HTTP.Method        		method;
	protected List<ApiParameterBuilder> parameters = new ArrayList<ApiParameterBuilder>();
	protected List<ApiResponseBuilder>  responses  = new ArrayList<ApiResponseBuilder>();
	protected List<String>              consumes   = new ArrayList<String>();
	protected List<String>              produces   = new ArrayList<String>();
	protected boolean           	    deprecated;

	public ApiOperationBuilder() {
		
	}
	
	public HTTP.Method getMethod() {
		return method;
	}

	public ApiOperationBuilder setMethod(HTTP.Method method) {
		this.method = method;
		return this;
	}

	public List<ApiParameterBuilder> getParameters() {
		return parameters;
	}
	
	public void addParameter(ApiParameterBuilder p) {
		parameters.add(p);
	}
	
	public List<ApiResponseBuilder> getResponses() {
		return responses;
	}
	
	public void addResponse(ApiResponseBuilder r) {
		responses.add(r);
	}
	
	public List<String> getConsumes() {
		return consumes;
	}
	
	public void addConsume(String mimeType){
		consumes.add(mimeType);
	}

	public List<String> getProduces() {
		return produces;
	}
	
	public void addProduce(String mimeType) {
		produces.add(mimeType);
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	@Override
    public ApiOperation build() {
		return new ApiOperation(name, title, summary, description, method, 
								Builders.buildList(parameters), 
								Builders.buildList(responses), 
								consumes.toArray(Arrays2.EMPTY_STRING_ARRAY), 
								produces.toArray(Arrays2.EMPTY_STRING_ARRAY), 
								deprecated, attrs);
    }
	
}
