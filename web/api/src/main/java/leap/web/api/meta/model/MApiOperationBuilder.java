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
import leap.web.api.meta.desc.OperationDesc;
import leap.web.route.Route;

public class MApiOperationBuilder extends MApiNamedWithDescBuilder<MApiOperation> {

    protected Route                      route;
	protected HTTP.Method        		 method;
    protected Set<String>                tags       = new LinkedHashSet<>();
	protected List<MApiParameterBuilder> parameters = new ArrayList<>();
	protected List<MApiResponseBuilder>  responses  = new ArrayList<>();
	protected Set<String>                consumes   = new LinkedHashSet<>();
	protected Set<String>                produces   = new LinkedHashSet<>();
    protected String[]                   permissions;
    protected boolean                    allowAnonymous;
	protected boolean           	     deprecated;
    protected OperationDesc              desc;

	public MApiOperationBuilder() {
		
	}

    public MApiOperationBuilder(Route route) {
        this.route       = route;
        this.permissions = route.getPermissions();

        if(null != route.getAllowAnonymous()) {
            this.allowAnonymous = route.getAllowAnonymous();
        }
    }

    public Route getRoute() {
        return route;
    }

    public HTTP.Method getMethod() {
		return method;
	}

	public MApiOperationBuilder setMethod(HTTP.Method method) {
		this.method = method;
		return this;
	}

    public Set<String> getTags() {
        return tags;
    }

    public void addTag(String tag){
        tags.add(tag);
    }

    public List<MApiParameterBuilder> getParameters() {
		return parameters;
	}

    public MApiParameterBuilder getParameter(String name) {
        for(MApiParameterBuilder p : parameters) {
            if(p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
	
	public void addParameter(MApiParameterBuilder p) {
		parameters.add(p);
	}
	
	public List<MApiResponseBuilder> getResponses() {
		return responses;
	}

    public MApiResponseBuilder getResponse(String name) {
        for(MApiResponseBuilder r : responses) {
            if(r.getName().equals(name)) {
                return r;
            }
        }
        return null;
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

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public void setDesc(OperationDesc desc){
        this.desc = desc;
        this.setSummary(desc.getSummary());
        this.setDescription(desc.getDescription());
    }

    public OperationDesc getDesc() {
        return desc;
    }

    @Override
    public MApiOperation build() {
		return new MApiOperation(name, title, summary, description, method,route,
                                tags.toArray(Arrays2.EMPTY_STRING_ARRAY),
								Builders.buildList(parameters), 
								Builders.buildList(responses), 
								consumes.toArray(Arrays2.EMPTY_STRING_ARRAY), 
								produces.toArray(Arrays2.EMPTY_STRING_ARRAY),
                                permissions,
                                allowAnonymous,
								deprecated, attrs);
    }
	
}
