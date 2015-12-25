/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.xml;


public class XmlValidationException extends XmlException {

	private static final long serialVersionUID = 5141320954015097550L;

	public XmlValidationException() {

	}

	public XmlValidationException(String message) {
		super(message);
	}

	public XmlValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlValidationException(Throwable cause) {
		super(cause);
	}

}
