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

import leap.lang.http.HTTP;
import leap.web.route.Route;

import java.util.List;
import java.util.Map;

public class MApiOperation extends MApiNamedWithDesc {

    protected final String          id;
    protected final Route           route;
    protected final HTTP.Method     method;
    protected final String[]        tags;
    protected final MApiParameter[] parameters;
    protected final MApiResponse[]  responses;
    protected final String[]        consumes;
    protected final String[]        produces;
    protected final MApiSecurity[]  security;
    protected final boolean         allowAnonymous;
    protected final boolean         allowClientOnly;
    protected final boolean         deprecated;

    public MApiOperation(String id, String name, String title, String summary, String description,
                         HTTP.Method method,
                         Route route,
                         String[] tags,
                         List<MApiParameter> parameters,
                         List<MApiResponse> responses,
                         String[] consumes,
                         String[] produces,
                         MApiSecurity[] security,
                         boolean allowAnonymous,
                         boolean allowClientOnly,
                         boolean deprecated, Map<String, Object> attrs) {
        super(name, title, summary, description, attrs);

        this.id = id;
        this.route = route;
        this.method = method;
        this.tags = tags;
        this.parameters = parameters.toArray(new MApiParameter[]{});
        this.responses = responses.toArray(new MApiResponse[]{});
        this.consumes = consumes;
        this.produces = produces;
        this.security = security;
        this.allowAnonymous = allowAnonymous;
        this.allowClientOnly = allowClientOnly;
        this.deprecated = deprecated;
    }

    /**
     * Returns the unique id of this operation.
     */
    public String getId() {
        return id;
    }

    /**
     * The route of this operation
     */
    public Route getRoute() {
        return route;
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
     * <p>
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
     * Returns the matched parameter of null if not exists.
     */
    public MApiParameter tryGetParameter(String name) {
        for(MApiParameter p : parameters) {
            if(p.getName().equals(name)) {
                return p;
            }
        }
        return null;
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
     * Returns the permissions required by this operation.
     */
    public MApiSecurity[] getSecurity() {
        return security;
    }

    /**
     * Returns true if this operation allows anonymous access.
     */
    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    /**
     * Returns true if this operation allows only client access.
     */
    public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    /**
     * Declares this operation to be deprecated.
     */
    public boolean isDeprecated() {
        return deprecated;
    }

}