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
package leap.web.json;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

import leap.lang.Strings;
import leap.lang.js.JS;
import leap.web.Request;
import leap.web.Response;
import leap.web.exception.BadRequestException;

public class Jsonp {

	protected Jsonp() {
		
	}

	public static void write(Request request, Response response, JsonConfig jc, Consumer<Writer> func) throws IOException {
		Writer writer = response.getWriter();
		
		if(jc.isJsonpEnabled()){
			String callback = request.getParameter(jc.getJsonpParameter());
			if(!Strings.isEmpty(callback)){
				if(!JS.isValidJavascriptFunction(callback)){
					throw new BadRequestException("Invalid jsonp callback : " + callback);
				}
				writer.write(callback);
				writer.write('(');
				func.accept(writer);
				writer.write(')');
				return;
			}
		}
		
		func.accept(writer);
	}
	
}
