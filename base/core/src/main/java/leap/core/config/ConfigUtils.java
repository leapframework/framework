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

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigUtils {

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
            if(key.startsWith(keyPrefix)) {
                props.put(key.substring(keyPrefix.length()), config.getProperty(key));
            }
        });

        if(null != extraConfig) {
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
        if(null == value) {
            return;
        }

        String[] parts = Strings.split(key, '.');
        if(parts.length == 1) {
            map.put(key, value);
        }else {
            Map parent = map;
            for(int i=0;i<parts.length-1;i++) {
                String name = parts[i];

                Object v = parent.get(name);
                if(v instanceof Map) {
                    parent = (Map)v;
                }else {
                    Map nested = new LinkedHashMap();
                    parent.put(name, nested);
                    parent = nested;
                    if(null != v) {
                        map.put(key(parts, i), v);
                    }
                }
            }
            parent.put(parts[parts.length - 1], value);
        }
    }

    protected static String key(String[] parts, int index) {
        StringBuilder s = new StringBuilder();
        for(int i=0;i<=index;i++) {
            if(i > 0) {
                s.append('.');
            }
            s.append(parts[i]);
        }
        return s.toString();
    }
}

