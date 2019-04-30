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

import leap.lang.Strings;
import leap.lang.json.JSON;
import leap.lang.resource.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;

public class YAML {

    /**
     * Parse the yaml content to {@link YamlValue}.
     */
    public static YamlValue parse(String string) throws YamlException {
        return YamlValue.of(decode(string));
    }

    /**
     * Parse the yaml content to {@link YamlValue}.
     */
    public static YamlValue parse(Reader reader) throws YamlException {
        return YamlValue.of(decode(reader));
    }

    /**
     * Decodes the yaml content to raw value.
     * <p>
     * <p/>
     * The raw value may be null, map, list or simpl value.
     */
    public static <T> T decode(String string) throws YamlException {
        return new Yaml().load(string);
    }

    /**
     * Decodes the yaml content to raw value.
     * <p>
     * <p/>
     * The raw value may be null, map, list or simpl value.
     */
    public static <T> T decode(Reader reader) throws YamlException {
        return new Yaml().load(reader);
    }

    /**
     * Decodes the value by yaml or json format.
     */
    public static <T> T decodeYamlOrJson(Resource resource) {
        String filename = resource.getFilename();
        if (Strings.endsWithIgnoreCase(filename, ".yml") || Strings.endsWithIgnoreCase(filename, ".yaml")) {
            return decode(resource.getContent());
        } else if (Strings.endsWithIgnoreCase(filename, ".json")) {
            return JSON.decode(resource.getContent());
        } else {
            throw new IllegalStateException("The file '" + filename + "' must be yml or json format");
        }
    }

    protected YAML() {

    }

}