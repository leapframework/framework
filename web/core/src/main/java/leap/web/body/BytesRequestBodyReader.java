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
package leap.web.body;

import java.lang.reflect.Type;

import leap.lang.Arrays2;
import leap.lang.io.IO;
import leap.web.Request;

public class BytesRequestBodyReader implements RequestBodyReader {
	@Override
	public boolean canReadRequestBody(Class<?> type, Type genericType) {
		return Arrays2.EMPTY_BYTE_ARRAY.getClass().equals(type);
	}

	@Override
	public Object readRequestBody(Request request, Class<?> type, Type genericType) throws Throwable {
		return IO.readByteArray(request.getInputStream());
	}

}