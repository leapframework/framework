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
package leap.web.api.meta;

import leap.lang.exception.ObjectNotFoundException;
import leap.web.api.meta.model.*;

import java.util.Collections;
import java.util.Map;

/**
 * This is the root document object for the API specification.
 */
public class ApiMetadata extends MApiNamedWithDesc {

    protected final String                    termsOfService;
    protected final MApiContact               concat;
    protected final String                    version;
    protected final String                    host;
    protected final String                    basePath;
    protected final String[]                  protocols;
    protected final String[]                  consumes;
    protected final String[]                  produces;
    protected final Map<String, MApiResponse> responses;
    protected final Map<String, MApiPath>     paths;
    protected final Map<String, MApiModel>    models;
    protected final MPermission[]             permissions;
    protected final MApiSecurityDef[]         securityDefs;

	public ApiMetadata(String name, String title, String summary, String description,
                       String termsOfService, MApiContact concat, String version,
                       String host, String basePath, String[] protocols, String[] consumes, String[] produces,
                       Map<String, MApiResponse> responses,
                       Map<String, MApiPath> paths,
                       Map<String, MApiModel> models,
                       MPermission[] permissions,
                       MApiSecurityDef[] securityDefs,
                       Map<String, Object> attrs)  {
		super(name, title, summary, description, attrs);
	    this.termsOfService = termsOfService;
	    this.concat = concat;
	    this.version = version;
		this.host = host;
		this.basePath = basePath;
		this.protocols = protocols;
		this.consumes = consumes;
		this.produces = produces;
        this.responses = Collections.unmodifiableMap(responses);
		this.paths    = Collections.unmodifiableMap(paths);
		this.models   = Collections.unmodifiableMap(models);
        this.permissions = permissions;
		this.securityDefs = securityDefs;
	}
	
	/**
	 * The terms of service for the API.
	 */
	public String getTermsOfService() {
		return termsOfService;
	}
	
	/**
	 * The contact information for the exposed API.
	 */
	public MApiContact getConcat() {
		return concat;
	}
	
	/**
	 * <b>Required</b>. The version string of the API.
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * The host (name or ip) serving the API. 
	 * 
	 * <p>
	 * This MUST be the host only and does not include the scheme nor sub-paths. 
	 * 
	 * <p>
	 * It MAY include a port. If the host is not included, the host serving the documentation is to be used (including the port).
	 */
	public String getHost() {
		return host;
	}

	/**
	 * The base path on which the API is served, which is relative to the host. 
	 * 
	 * <p>
	 * If it is not included, the API is served directly under the host.
	 * 
	 * <p>
	 * The value MUST start with a leading slash (/).
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * The transfer protocol of the API. Values MUST be from the list: "http", "https". 
	 * 
	 * <p>
	 * If the schemes is not included, the default scheme to be used is the one used to access the api definition itself.
	 */
	public String[] getProtocols() {
		return protocols;
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
     * An object to hold responses that can be used across operations.
     */
    public Map<String, MApiResponse> getResponses() {
        return responses;
    }

    /**
	 * Required. The available paths and operations for the API.
	 */
	public Map<String,MApiPath> getPaths() {
		return paths;
	}

    /**
     * Returns all the models.
     */
	public Map<String,MApiModel> getModels() {
		return models;
	}

    public MApiModel getModel(String name) {
        MApiModel m = models.get(name);
        if(null == m) {
            throw new ObjectNotFoundException("The model '" + name + "' not exists!");
        }
        return m;
    }

    public MApiModel getModel(Class<?> type) {
        for(MApiModel m : models.values()) {
            if(type.equals(m.getJavaType())) {
                return m;
            }
        }
        throw new ObjectNotFoundException("No api model of type '" + type + "'");
    }

    /**
     * Returns all the permissions required by this api.
     */
    public MPermission[] getPermissions() {
        return permissions;
    }

    public MApiSecurityDef[] getSecurityDefs() {
        return securityDefs;
    }
}
