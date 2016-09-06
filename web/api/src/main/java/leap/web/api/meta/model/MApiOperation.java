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

import java.util.List;
import java.util.Map;

import leap.lang.http.HTTP;

public class MApiOperation extends MApiNamedWithDesc {
	
	protected final HTTP.Method     method;
    protected final String[]        tags;
	protected final MApiParameter[] parameters;
	protected final MApiResponse[]  responses;
	protected final String[]        consumes;
	protected final String[]        produces;
	protected final boolean         deprecated;

	public MApiOperation(String name, String title, String summary, String description,
                         HTTP.Method method,
                         String[] tags,
                         List<MApiParameter> parameters,
                         List<MApiResponse> responses,
                         String[] consumes,
                         String[] produces,
                         boolean deprecated, Map<String, Object> attrs) {
		
	    super(name, title, summary, description, attrs);
	    
		this.method      = method;
        this.tags        = tags;
		this.parameters  = parameters.toArray(new MApiParameter[]{});
		this.responses   = responses.toArray(new MApiResponse[]{});
		this.consumes    = consumes;
		this.produces    = produces;
		this.deprecated  = deprecated;
	}
	
	/**
	 * The short description of this operation.
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * The long description of this operation.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the allowed http method on this operation.
	 */
	public HTTP.Method getMethod() {
		return method;
	}

    /**
     * A list of tags for API documentation control.
     *
     * Tags can be used for logical grouping of operations by resources or any other qualifier
     */
    public String[] getTags() {
        return tags;
    }

    /**
	 * Returns an array of {@link MApiParameter} of this operation.
	 */
	public MApiParameter[] getParameters() {
		return parameters;
	}

	/**
	 * Returns an array of {@link MApiResponse} of this operation.
	 */
	public MApiResponse[] getResponses() {
		return responses;
	}
	
	/**
	 * A list of MIME types the APIs can consume. This is global to all APIs but can be overridden on specific API calls.
	 */
	public String[] getConsumes() {
		return consumes;
	}

	/**
	 * A list of MIME types the APIs can produce. This is global to all APIs but can be overridden on specific API calls.
	 */
	public String[] getProduces() {
		return produces;
	}
	/**
	 * Declares this operation to be deprecated.
	 */
	public boolean isDeprecated() {
		return deprecated;
	}

}