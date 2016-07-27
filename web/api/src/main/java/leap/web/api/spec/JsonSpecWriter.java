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
package leap.web.api.spec;

import java.io.IOException;

import leap.lang.http.MimeTypes;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.lang.json.JsonWriterCreator;
import leap.lang.naming.NamingStyle;
import leap.web.api.meta.model.ApiMetadata;

public abstract class JsonSpecWriter implements ApiSpecWriter {
	
	protected NamingStyle propertyNamingStyle;
	
	public NamingStyle getPropertyNamingStyle() {
		return propertyNamingStyle;
	}

	public void setPropertyNamingStyle(NamingStyle namingStyle) {
		this.propertyNamingStyle = namingStyle;
	}

	@Override
	public String getContentType() {
		return MimeTypes.APPLICATION_JSON_UTF8;
	}

	@Override
	public void write(ApiMetadata m, Appendable out) throws IOException {
		write(m, createJsonWriter(out));
	}
	
	protected JsonWriter createJsonWriter(Appendable out) {
		JsonWriterCreator writer = JSON.writer(out);
		
		if(null != propertyNamingStyle) {
			writer.setNamingStyle(propertyNamingStyle);
		}
		
		return writer.create();
	}
	
	protected String propertyName(String name) {
		return null == propertyNamingStyle ? name : propertyNamingStyle.of(name);
	}
	
	protected abstract void write(ApiMetadata m, JsonWriter w); 

}