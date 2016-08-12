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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import leap.lang.Arrays2;
import leap.lang.Builders;
import leap.lang.http.HTTP;

public class MApiOperationBuilder extends MApiNamedWithDescBuilder<MApiOperation> {
	
	protected HTTP.Method        		 method;
	protected List<MApiParameterBuilder> parameters = new ArrayList<>();
	protected List<MApiResponseBuilder>  responses  = new ArrayList<>();
	protected Set<String>                consumes   = new LinkedHashSet<>();
	protected Set<String>                produces   = new LinkedHashSet<>();
	protected boolean           	     deprecated;

	public MApiOperationBuilder() {
		
	}
	
	public HTTP.Method getMethod() {
		return method;
	}

	public MApiOperationBuilder setMethod(HTTP.Method method) {
		this.method = method;
		return this;
	}

	public List<MApiParameterBuilder> getParameters() {
		return parameters;
	}
	
	public void addParameter(MApiParameterBuilder p) {
		parameters.add(p);
	}
	
	public List<MApiResponseBuilder> getResponses() {
		return responses;
	}
	
	public void addResponse(MApiResponseBuilder r) {
		responses.add(r);
	}
	
	public Set<String> getConsumes() {
		return consumes;
	}
	
	public void addConsume(String mimeType){
		consumes.add(mimeType);
	}

	public Set<String> getProduces() {
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
    public MApiOperation build() {
		return new MApiOperation(name, title, summary, description, method,
								Builders.buildList(parameters), 
								Builders.buildList(responses), 
								consumes.toArray(Arrays2.EMPTY_STRING_ARRAY), 
								produces.toArray(Arrays2.EMPTY_STRING_ARRAY), 
								deprecated, attrs);
    }
	
}
