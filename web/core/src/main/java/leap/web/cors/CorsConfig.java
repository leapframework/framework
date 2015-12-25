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

public interface CorsConfig {
	
	String CONFIX_PREFIX = "webmvc.cors";

	boolean isAllowAnyOrigin();

	boolean isAllowAnyMethod();

	boolean isAllowAnyHeader();

	String[] getAllowedOrigins();

	String[] getAllowedMethods();

	String[] getAllowedHeaders();

	String[] getExposedHeaders();

	boolean isSupportsCredentials();

	int getPreflightMaxAge();

	boolean isOriginAllowed(String origin);

	boolean isMethodAllowed(String method);

	boolean isHeaderAllowedIgnoreCase(String header);

	boolean hasExposedHeaders();

	String getExposedHeadersValue();

}