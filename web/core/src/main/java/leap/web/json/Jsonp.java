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

import leap.lang.Iterators;
import leap.lang.Strings;
import leap.lang.js.JS;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.web.Request;
import leap.web.Response;
import leap.web.exception.BadRequestException;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

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
				if(jc.isJsonpResponseHeaders()){
					JsonWriter jw = JSON.createWriter(writer);
					writer.write(',');
					jw.startObject();
					response.getHeaderNames().forEach(s -> {
						Iterator<String> iterator = jc.getJsonpAllowResponseHeaders().iterator();
						boolean allow = Iterators.any(iterator,allowHeader -> {
							if("*".equals(allowHeader)){
								return true;
							}else {
								return Strings.equals(s,allowHeader);
							}
						});
						if(allow){
							Collection<String> h = response.getHeaders(s);
							if(!h.isEmpty()){
								if(h.size() == 1){
									jw.property(s,h.iterator().next());
								}else {
									jw.array(h.iterator());
								}
							}
						}
					});
					jw.endObject();
				}
				writer.write(')');
				return;
			}
		}
		
		func.accept(writer);
	}
	
}
