/*
 * Copyright 2014 the original author or authors.
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
package leap.web.format;

import leap.lang.http.MimeType;
import leap.web.Request;
import leap.web.action.Action;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class AbstractRequestFormat extends AbstractHttpFormat implements RequestFormat {
	
	public AbstractRequestFormat() {
	    super();
    }

	public AbstractRequestFormat(MimeType... supportedMediaTypes) {
	    super(supportedMediaTypes);
    }

	@Override
    public boolean supports(Action action) {
	    return true;
    }

	@Override
    public boolean supportsRequestBody() {
	    return false;
    }

	@Override
    public Object readRequestBody(Request request) throws IOException, IllegalStateException {
		throw new IllegalStateException("This format '" + name + "' does not supports request body");
	}
}