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

import java.util.Map;

import leap.lang.New;

/**
 * This is the root document object for the API specification.
 */
public class ApiMetadata extends ApiNamedWithDesc {
	
	protected final String    	  		 termsOfService;
	protected final ApiConcat 	  		 concat;
	protected final String    	  		 version;
	protected final String    	  		 host;
	protected final String    	  		 basePath;
	protected final String[]  	  		 protocols;
	protected final String[]  	  	     consumes;
	protected final String[]  	  		 produces;
	protected final Map<String,ApiPath>  paths;
	protected final Map<String,ApiModel> models;
	protected final ApiSecurtyDef[]      securityDefs;

	public ApiMetadata(String name, String title, String summary, String description, 
					 String termsOfService, ApiConcat concat, String version, 
					 String host, String basePath, String[] protocols, String[] consumes, String[] produces, 
					 Map<String, ApiPath> paths, 
					 Map<String, ApiModel> models,
					 ApiSecurtyDef[] securityDefs,
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
		this.paths    = New.unmodifiableHashMap(paths);
		this.models   = New.unmodifiableHashMap(models);
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
	public ApiConcat getConcat() {
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
	 * Required. The available paths and operations for the API.
	 */
	public Map<String,ApiPath> getPaths() {
		return paths;
	}

	public Map<String,ApiModel> getModels() {
		return models;
	}

    public ApiSecurtyDef[] getSecurityDefs() {
        return securityDefs;
    }
}
