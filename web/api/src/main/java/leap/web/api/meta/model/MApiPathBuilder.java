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
import leap.lang.exception.ObjectNotFoundException;
import leap.web.route.Route;

public class MApiPathBuilder extends MApiObjectBuilder<MApiPath> {

	protected String       		         pathTemplate;
	protected List<MApiOperationBuilder> operations = new ArrayList<MApiOperationBuilder>();

	public MApiPathBuilder() {
		
	}

    public String getPathTemplate() {
		return pathTemplate;
	}
	
	public MApiPathBuilder setPathTemplate(String pathTemplate) {
		this.pathTemplate = pathTemplate;
		return this;
	}

	public List<MApiOperationBuilder> getOperations() {
		return operations;
	}

    public MApiOperationBuilder getOperation(String method) {
        for(MApiOperationBuilder op : operations) {
            if(op.getMethod().name().equalsIgnoreCase(method)) {
                return op;
            }
        }
        return null;
    }
	
	public MApiPathBuilder addOperation(MApiOperationBuilder operation) {
		Args.notNull(operation, "operation");
		operations.add(operation);
		return this;
	}

	public MApiPathBuilder addOperations(Collection<MApiOperationBuilder> operations) {
		if(null != operations) {
			this.operations.addAll(operations);
		}
		return this;
	}

	@Override
    public MApiPath build() {
	    return new MApiPath(pathTemplate, Builders.buildList(operations), attrs);
    }
	
}