/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.core.config;

import leap.core.AppConfig;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.yaml.YAML;
import leap.lang.yaml.YamlValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ConfigUtils {

    public static boolean isJsonOrYaml(Resource resource) {
        if (null == resource || !resource.exists() || resource.isDirectory()) {
            return false;
        }
        String filename = resource.getFilename();
        if (Strings.endsWithIgnoreCase(filename, ".json")
                || Strings.endsWithIgnoreCase(filename, ".yml") || Strings.endsWithIgnoreCase(filename, ".yaml")) {
            return true;
        }
        return false;
    }

    public static boolean isJsonOrYaml(Resource resource, String name) {
        return isNameMatchedJsonOrYaml(resource, (n) -> n.equals(name));
    }

    public static boolean isNameMatchedJsonOrYaml(Resource resource, Function<String, Boolean> nameMatcher) {
        if (!isJsonOrYaml(resource)) {
            return false;
        }
        String name = Paths.getFileNameWithoutExtension(resource.getFilename());
        if (nameMatcher.apply(name)) {
            return true;
        }
        return false;
    }

    public static Map<String, Object> decodeMap(Resource resource) {
        if (null == resource || !resource.exists() || resource.isDirectory()) {
            return null;
        }
        return decodeMap(resource.getFilename(), resource.getContent());
    }

    public static Map<String, Object> decodeMap(String filename, String content) {
        if (Strings.isEmpty(content)) {
            return null;
        }
        if (Strings.endsWithIgnoreCase(filename, ".json")) {
            return JSON.decodeMap(content);
        } else if (Strings.endsWithIgnoreCase(filename, ".yaml") || Strings.endsWithIgnoreCase(filename, ".yml")) {
            YamlValue v = YAML.parse(content);
            return v.isNull() ? null : v.asMap();
        } else {
            return null;
        }
    }

    public static Object decodeObj(Resource resource) {
        if (null == resource || !resource.exists() || resource.isDirectory()) {
            return null;
        }
        return decodeObj(resource.getFilename(), resource.getContent());
    }

    public static Object decodeObj(String filename, String content) {
        if (Strings.isEmpty(content)) {
            return null;
        }
        if (Strings.endsWithIgnoreCase(filename, ".json")) {
            return JSON.decode(content);
        } else if (Strings.endsWithIgnoreCase(filename, ".yaml") || Strings.endsWithIgnoreCase(filename, ".yml")) {
            return YAML.parse(content).raw();
        } else {
            return null;
        }
    }

    public static List<Object> decodeList(Resource resource) {
        if (null == resource || !resource.exists() || resource.isDirectory()) {
            return null;
        }
        return decodeList(resource.getFilename(), resource.getContent());
    }

    public static List<Object> decodeList(String filename, String content) {
        if (Strings.isEmpty(content)) {
            return null;
        }
        if (Strings.endsWithIgnoreCase(filename, ".json")) {
            return JSON.parse(content).asList();
        } else if (Strings.endsWithIgnoreCase(filename, ".yaml") || Strings.endsWithIgnoreCase(filename, ".yml")) {
            return YAML.parse(content).asList();
        } else {
            return null;
        }
    }

    public static <T> T decodeJsonMap(Map map, Class<T> type) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        JSON.checkMissingProperties(type, map);
        return (T) Converts.toBean(map, type);
    }

    public static Map<String,Object> toObjectProperties(Map<String, Object> map) {
        return toObjectProperties(map, "");
    }

    public static Map<String,Object> toObjectProperties(Map<String, Object> map, String prefix) {
        Map<String, Object> props = new LinkedHashMap<>();
        putObjectProperties(prefix, map, props);
        return props;
    }

    protected static void putObjectProperties(String prefix, Map map, Map<String, Object> props) {
        map.forEach((k,v) -> {
            if(v instanceof Map) {
                putObjectProperties(prefix + k + ".", (Map)v, props);
            }else {
                props.put(prefix + k, v);
            }
        });
    }

    public static Map<String, String> toProperties(Map<String, Object> map) {
        return toProperties(map, "");
    }

    public static Map<String, String> toProperties(Map<String, Object> map, String prefix) {
        Map<String, String> props = new LinkedHashMap<>();
        putProperties(prefix, map, props);
        return props;
    }

    private static void putProperties(String prefix, Map map, Map<String, String> props) {
        map.forEach((k, v) -> {
            if (v instanceof Map) {
                putProperties(prefix + k + ".", (Map) v, props);
            } else {
                props.put(prefix + k, Converts.toString(v));
            }
        });
    }

    public static String extractKeyPrefix(String key) {
        int index = key.indexOf(".");
        if(index > 0) {
            return key.substring(0, index);
        }else {
            return null;
        }
    }

    public static String removeKeyPrefix(String key, String prefix) {
        return key.substring(prefix.length() + 1);
    }

    public static Map<String, Object> extractMap(AppConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();

        config.getPropertyNames().forEach(key -> {
            String value = config.getProperty(key);
            extractMap(map, key, value);
        });

        return map;
    }

    public static Map<String, Object> extractMap(AppConfig config, String prefix) {
        return extractMap(config, null, prefix);
    }

    public static Map<String, Object> extractMap(AppConfig config, Map<String, String> extraConfig, String prefix) {
        Map<String, String> props = new LinkedHashMap<>();

        final String keyPrefix = prefix.endsWith(".") ? prefix : prefix + ".";

        config.getPropertyNames().forEach(key -> {
            if (key.startsWith(keyPrefix)) {
                props.put(key.substring(keyPrefix.length()), config.getProperty(key));
            }
        });

        if (null != extraConfig) {
            extraConfig.forEach((key, value) -> {
                if (key.startsWith(keyPrefix)) {
                    props.put(key.substring(keyPrefix.length()), value);
                }
            });
        }

        Map<String, Object> map = new LinkedHashMap<>();
        props.forEach((k, v) -> {
            extractMap(map, k, v);
        });
        return map;
    }

    protected static void extractMap(Map map, String key, String value) {
        if (null == value) {
            return;
        }

        String[] parts = Strings.split(key, '.');
        if (parts.length == 1) {
            map.put(key, value);
        } else {
            Map parent = map;
            for (int i = 0; i < parts.length - 1; i++) {
                String name = parts[i];

                Object v = parent.get(name);
                if (v instanceof Map) {
                    parent = (Map) v;
                } else {
                    Map nested = new LinkedHashMap();
                    parent.put(name, nested);
                    parent = nested;
                    if (null != v) {
                        map.put(key(parts, i), v);
                    }
                }
            }
            parent.put(parts[parts.length - 1], value);
        }
    }

    protected static String key(String[] parts, int index) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i <= index; i++) {
            if (i > 0) {
                s.append('.');
            }
            s.append(parts[i]);
        }
        return s.toString();
    }
}

