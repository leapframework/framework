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
package leap.web.multipart;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.Part;

import leap.lang.Charsets;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;

public class Multiparts {

	public static String readString(Part part) {
		if(null == part || part.getSize() == 0) {
			return Strings.EMPTY;
		}
		
		try {
	        try(InputStream in = part.getInputStream()) {
	        	if(!Strings.isEmpty(part.getContentType())) {
	        		MimeType mimeType = MimeTypes.parse(part.getContentType());
	        		
	        		if(!Strings.isEmpty(mimeType.getCharset())) {
	        			return IO.readString(in, Charsets.forName(mimeType.getCharset()));
	        		}
	        	}
	        	
	        	return IO.readString(in, Charsets.UTF_8);
	        }
        } catch (IOException e) {
        	throw new NestedIOException("Error reading string from part '" + part.getName() + "', " + e.getMessage(), e);
        }
	}
	
	protected Multiparts() {
		
	}
}
