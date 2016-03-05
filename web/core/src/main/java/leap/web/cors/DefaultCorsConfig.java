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
package leap.web.cors;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.lang.Arrays2;
import leap.lang.Strings;


@Configurable(prefix=CorsConfig.CONFIX_PREFIX)
public class DefaultCorsConfig implements CorsConfig, CorsConfigurator {
	public static final String CONF_ALLOWED_ORIGINS      = "allowed-origins";
	public static final String CONF_ALLOWED_METHODS      = "allowed-methods";
	public static final String CONF_ALLOWED_HEADERS      = "allowed-headers";
	public static final String CONF_EXPOSED_HEADERS      = "exposed-headers";
	public static final String CONF_SUPPORTS_CREDENTIALS = "supports-credentials";
	public static final String CONF_PREFLIGHT_MAXAGE	 = "preflight-maxage";
	
    public static final String[]  DEFAULT_ALLOWED_ORIGINS      = new String[]{"*"};
    public static final String[]  DEFAULT_ALLOWED_METHODS      = new String[]{"GET","POST","HEAD","OPTIONS"};
    public static final int       DEFAULT_PREFLIGHT_MAXAGE     = 3600; // seconds
    public static final boolean   DEFAULT_SUPPORTS_CREDENTIALS = true;
    public static final String[]  DEFAULT_EXPOSED_HEADERS      = Arrays2.EMPTY_STRING_ARRAY;
    public static final boolean   DEFAULT_DECORATE_REQUEST     = true;	
    public static final String[]  DEFAULT_ALLOWED_HEADERS      = new String[]{"Origin","Accept","X-Requested-With",
    																		  "Content-Type",
            												   				  "Access-Control-Request-Method",
            												   				  "Access-Control-Request-Headers"};

    protected String[] allowedOrigins;
	protected String[] allowedMethods;
	protected String[] allowedHeaders;
	protected String[] exposedHeaders;
	protected boolean  supportsCredentials;
	protected int	   preflightMaxAge;
	
	private boolean  allowAnyOrigin;
	private boolean  allowAnyMethod;
	private boolean  allowAnyHeader;
	private String	 exposedHeadersValue;
	
	public DefaultCorsConfig() {
		this(DEFAULT_ALLOWED_ORIGINS,
			 DEFAULT_ALLOWED_METHODS,
			 DEFAULT_ALLOWED_HEADERS,
			 DEFAULT_EXPOSED_HEADERS,
			 DEFAULT_SUPPORTS_CREDENTIALS,
			 DEFAULT_PREFLIGHT_MAXAGE);	
	}

	public DefaultCorsConfig(String[] allowedOrigins, 
					  String[] allowedMethods, 
					  String[] allowedHeaders, 
					  String[] exposedHeaders,
					  boolean  supportsCredentials,
					  int	   preflightMaxAge) {
	    this.allowedOrigins      = allowedOrigins;
	    this.allowedMethods      = allowedMethods;
	    this.allowedHeaders      = allowedHeaders;
	    this.exposedHeaders      = exposedHeaders;
	    this.supportsCredentials = supportsCredentials;
	    this.preflightMaxAge     = preflightMaxAge;
	    
	    resetAllowAnyOrigin();
	    resetAllowAnyMethod();
	    resetAllowAnyHeader();
	    resetExposedHeadersValue();
    }
	
	@Override
    public CorsConfig config() {
	    return this;
    }

	@Override
    public boolean isAllowAnyOrigin() {
		return allowAnyOrigin;
	}
	
	@Override
    public boolean isAllowAnyMethod() {
		return allowAnyMethod;
	}

	@Override
    public boolean isAllowAnyHeader() {
		return allowAnyHeader;
	}

	@Override
    public String[] getAllowedOrigins() {
		return allowedOrigins;
	}

	@Override
    public String[] getAllowedMethods() {
		return allowedMethods;
	}

	@Override
    public String[] getAllowedHeaders() {
		return allowedHeaders;
	}

	@Override
    public String[] getExposedHeaders() {
		return exposedHeaders;
	}

	@Override
    public boolean isSupportsCredentials() {
		return supportsCredentials;
	}
	
	@Override
    public int getPreflightMaxAge() {
		return preflightMaxAge;
	}

	@Override
	@ConfigProperty
    public void setAllowedOrigins(String[] allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
		resetAllowAnyOrigin();
	}

	@Override
	@ConfigProperty
    public void setAllowedMethods(String[] allowedMethods) {
		this.allowedMethods = allowedMethods;
		resetAllowAnyMethod();
	}

	@Override
	@ConfigProperty
    public void setAllowedHeaders(String[] allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
		resetAllowAnyHeader();
	}

	@Override
	@ConfigProperty
    public void setExposedHeaders(String[] exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
		resetExposedHeadersValue();
	}

	@Override
	@ConfigProperty
    public void setSupportsCredentials(boolean supportsCredentials) {
		this.supportsCredentials = supportsCredentials;
	}
	
	@Override
	@ConfigProperty
    public void setPreflightMaxAge(int preflightMaxAge) {
		this.preflightMaxAge = preflightMaxAge;
	}
	
	@Override
    public boolean isOriginAllowed(String origin) {
		return isAllow(allowAnyOrigin, allowedOrigins, origin, false);
	}
	
	@Override
    public boolean isMethodAllowed(String method) {
		return isAllow(allowAnyMethod, allowedMethods, method, false);
	}
	
	@Override
    public boolean isHeaderAllowedIgnoreCase(String header) {
		return isAllow(allowAnyHeader, allowedHeaders, header, true);
	}
	
	@Override
    public boolean hasExposedHeaders() {
		return null != exposedHeadersValue;
	}

	@Override
    public String getExposedHeadersValue() {
		return exposedHeadersValue;
	}

	protected void resetAllowAnyOrigin() {
		this.allowAnyOrigin = isAny(allowedOrigins);
	}
	
	protected void resetAllowAnyMethod() {
		this.allowAnyMethod = isAny(allowedMethods);
	}
	
	protected void resetAllowAnyHeader() {
		this.allowAnyHeader = isAny(allowedHeaders);
	}
	
	protected void resetExposedHeadersValue() {
		if(null == exposedHeaders || exposedHeaders.length == 0) {
			this.exposedHeadersValue = null;
		}else{
			this.exposedHeadersValue = Strings.join(exposedHeaders, ',');
		}
	}
	
	protected boolean isAllow(boolean any, String[] values, String value, boolean ignorecase) {
		if(any) {
			return true;
		}
		
		if(null != values){
			for(String v : values) {
				if( ignorecase ? v.equalsIgnoreCase(value)  : v.equals(value)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected boolean isAny(String[] values) {
		if(null == values || values.length == 0) {
			return true;
		}
		
		if(values.length == 1 && values[0].equals("*")) {
			return true;
		}
		
		return false;
	}
}