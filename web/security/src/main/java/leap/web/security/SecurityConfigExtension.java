/*
 * Copyright 2013 the original author or authors.
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
package leap.web.security;

import leap.core.config.AppConfigContext;
import leap.core.AppConfigException;
import leap.core.config.AppConfigProcessor;
import leap.lang.annotation.Internal;
import leap.lang.xml.XmlReader;

@Internal
public class SecurityConfigExtension implements AppConfigProcessor {
	
	public static final String NAMESPACE_URI = "http://www.leapframework.org/schema/security/config";
	
	//private static final String CONFIG_ELEMENT = "config"; //sec:config

	@Override
    public String getNamespaceURI() {
	    return NAMESPACE_URI;
    }

	@Override
    public void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException {
	    // TODO implement SecurityExtension.processElement
		
    }

}