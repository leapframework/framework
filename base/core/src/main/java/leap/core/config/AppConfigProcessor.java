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
package leap.core.config;

import leap.core.AppConfigException;
import leap.lang.xml.XmlReader;

public interface AppConfigProcessor {
	
	/**
	 * Returns the namespace uri of this extension.
	 * 
	 * i.e. http://sample.com/config/extension
	 */
	String getNamespaceURI();

	/**
	 * Processes current xml element in the {@link XmlReader}.
	 */
	void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException;
	
}