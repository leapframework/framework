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
package leap.lang.yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import leap.lang.exception.NestedIOException;

public class YAML {
	
	public static YamlValue parse(String string) throws YamlException {
		try {
	        return parse(new StringReader(string));
        } catch (IOException e) {
        	throw new NestedIOException(e);
        }
	}
	
	public static YamlValue parse(Reader reader) throws IOException,YamlException {
		return new YamlDecoder(reader).read();
	}

	protected YAML() {
		
	}

}