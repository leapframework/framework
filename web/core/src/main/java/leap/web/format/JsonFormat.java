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

import leap.core.annotation.Inject;
import leap.core.validation.annotations.NotNull;
import leap.core.validation.annotations.Required;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;
import leap.lang.json.JsonValue;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Content;
import leap.web.Contents;
import leap.web.Request;
import leap.web.Response;
import leap.web.action.ActionContext;
import leap.web.action.Argument;
import leap.web.json.JsonConfig;
import leap.web.json.Jsonp;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JsonFormat extends AbstractRequestFormat implements ResponseFormat,RequestFormat {

	private static final Log log = LogFactory.get(JsonFormat.class);
	
	@NotNull
	@Inject(name="json")
	protected FormatWriter writer;
	
	@NotNull
	@Inject
	protected JsonConfig jsonConfig;
	
	public JsonFormat() {
		super(MimeTypes.APPLICATION_JSON_TYPE);
	}

	@Override
    public boolean supportsRequestBody() {
		return true;
	}

	@Override
    public Object readRequestBody(Request request, Argument argument) throws IOException, IllegalStateException {
		JsonValue jsonObject;
		try {
            if(log.isTraceEnabled()) {
                String json = IO.readString(request.getReader());

                log.trace("Json request body : \n{}", json);

				if(Strings.isEmpty(json)){
					return null;
				}

                jsonObject = leap.lang.json.JSON.parse(json);
            }else{
                jsonObject = leap.lang.json.JSON.parse(request.getReader());
            }
        } catch (Exception e) {
        	throw new InvalidFormatContentException("Error reading 'json' request body, " + e.getMessage(), e);
        }
		
        return jsonObject.raw();
	}

    @Override
    public Content getContent(ActionContext context, Object value) throws Exception {
		return new Contents.AbstractTextContent() {
			
			@Override
			public String getContentType(Request request) throws Exception {
				return Contents.createContentType(request, MimeTypes.APPLICATION_JSON);
			}
			
			@Override
			protected void doRender(Request request, Response response) throws Exception {
				Jsonp.write(request, response, jsonConfig, (w) -> {
					try {
	                    writer.write(context, value, w);
                    } catch (Exception e) {
                    	throw Exceptions.uncheck(e);
                    }
				});
			}

			@Override
            public String toString() {
				return "json:" + (null == value ? "null" : value.getClass().getSimpleName());
            }
		};
    }

}