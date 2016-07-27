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
import java.util.Collection;
import java.util.List;

import leap.core.web.path.PathTemplate;
import leap.lang.Args;
import leap.lang.Builders;

public class ApiPathBuilder extends ApiObjectBuilder<ApiPath> {
	
	protected PathTemplate       		pathTemplate;
	protected List<ApiOperationBuilder> operations = new ArrayList<ApiOperationBuilder>();

	public ApiPathBuilder() {
		
	}

	public PathTemplate getPathTemplate() {
		return pathTemplate;
	}
	
	public ApiPathBuilder setPathTemplate(PathTemplate pathTemplate) {
		this.pathTemplate = pathTemplate;
		return this;
	}

	public List<ApiOperationBuilder> getOperations() {
		return operations;
	}
	
	public ApiPathBuilder addOperation(ApiOperationBuilder operation) {
		Args.notNull(operation, "operation");
		operations.add(operation);
		return this;
	}

	public ApiPathBuilder addOperations(Collection<ApiOperationBuilder> operations) {
		if(null != operations) {
			this.operations.addAll(operations);
		}
		return this;
	}

	@Override
    public ApiPath build() {
	    return new ApiPath(pathTemplate, Builders.buildList(operations), attrs);
    }
	
}