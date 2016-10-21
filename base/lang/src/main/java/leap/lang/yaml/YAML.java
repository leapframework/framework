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

import java.io.Reader;
import java.io.StringReader;

public class YAML {

    /**
     * Parse the yaml content to {@link YamlValue}.
     */
	public static YamlValue parse(String string) throws YamlException {
        return parse(new StringReader(string));
	}

    /**
     * Parse the yaml content to {@link YamlValue}.
     */
	public static YamlValue parse(Reader reader) throws YamlException {
		return YamlValue.of(new YamlDecoder(reader).decode());
	}

    /**
     * Decodes the yaml content to raw value.
     *
     * <p/>
     * The raw value may be null, map, list or simpl value.
     */
    public static <T> T decode(String string) throws YamlException {
        return (T)new YamlDecoder(new StringReader(string)).decode();
    }

    /**
     * Decodes the yaml content to raw value.
     *
     * <p/>
     * The raw value may be null, map, list or simpl value.
     */
    public static <T> T decode(Reader reader) throws YamlException {
        return (T)new YamlDecoder(reader).decode();
    }

	protected YAML() {
		
	}

}