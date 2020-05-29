/*
 *  Copyright 2019 the original author or authors.
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

package leap.core.spring;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;
import leap.lang.resource.Resource;
import leap.lang.yaml.YAML;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractExprReaderBean {

    @Inject
    protected PropertyResolver propertyResolver;

    @Inject
    protected ExpressionFactory expressionFactory;

    protected Set<String> getDelayExprPaths() {
        return Collections.emptySet();
    }

    protected boolean supportsExpr() {
        return false;
    }

    protected Map<String, Object> processMap(Resource resource, Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return map;
        }
        if (null != resource) {
            readInclude(resource, map);
        }
        processPlaceholderOrExpr(resource, map);
        return map;
    }

    protected <T> T processMapAndConvert(Resource resource, Map<String, Object> map, Class<T> type) {
        processMap(resource, map);
        checkMissingProperties(type, resource.getDescription(), map);
        return Converts.convert(map, type);
    }

    protected <T> T processMapAndConvert(Object source, Map<String, Object> map, Class<T> type) {
        processMap(null, map);
        checkMissingProperties(type, source, map);
        return Converts.convert(map, type);
    }

    protected void readInclude(Resource resource, Map<String, Object> map) {
        String inc = (String) map.remove("@include");
        if (!Strings.isEmpty(inc)) {
            readInclude(resource, map, inc);
        }
    }

    protected void readInclude(Resource resource, Map<String, Object> root, String incPath) {
        root.putAll((Map) readIncludeValue(resource, incPath));
    }

    protected Object readIncludeValue(Resource resource, String inc) {
        try {
            Resource incRes = resolveIncludeResource(resource, inc);
            if (null == incRes || !incRes.exists()) {
                throw new IllegalStateException("The included file '" + inc + "' not found");
            }
            return YAML.decodeYamlOrJson(incRes);
        } catch (IOException e) {
            throw new UncheckedIOException("Err read include '" + inc + "', " + e.getMessage(), e);
        }
    }

    protected Resource resolveIncludeResource(Resource resource, String inc) throws IOException {
        return resource.createRelative(inc);
    }

    protected void checkMissingProperties(Class<?> type, Object source, Map<String, Object> map) {
        Set<String> missingProperties = JSON.resolveMissingProperties(type, map);

        if (!missingProperties.isEmpty()) {
            for (String p : missingProperties) {
                if (p.equals("$") || p.endsWith(".$")) {
                    continue;
                }
                throw new IllegalStateException("Invalid property '" + p + "' at '" + source + "'");
            }
        }
    }

    protected void processPlaceholderOrExpr(Resource resource, Map<String, Object> map) {
        processPlaceholderOrExpr(resource, null, map);
    }

    protected void processPlaceholderOrExpr(Resource resource, String parent, Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return;
        }

        String[] keys = map.keySet().toArray(new String[0]);
        for (String key : keys) {
            String path = parent == null ? key : (parent + "." + key);
            Object item = map.get(key);
            if (null != item) {
                Object value = processPlaceholderOrExpr(resource, path, item);
                if (item != value) {
                    map.put(key, value);
                }
            }
        }
    }

    protected Object processPlaceholderOrExpr(Resource resource, String path, Object v) {
        if (null == v) {
            return null;
        }

        if (null != resource && v instanceof String && ((String) v).startsWith("@include")) {
            String[] parts = Strings.splitWhitespaces((String) v);
            if (parts[0].equals("@include") && parts.length == 2) {
                v = readIncludeValue(resource, parts[1]);
            }
        }

        if (v instanceof String) {
            String s = (String) v;

            if (s.contains("${")) {
                s = propertyResolver.resolvePlaceholders(s);
            }

            if (supportsExpr() && !getDelayExprPaths().contains(path) && s.contains("#{") && s.contains("}")) {
                return expressionFactory.createExpression(s).getValue();
            }

            return s != v ? s : v;
        }

        if (v instanceof Map) {
            if (!getDelayExprPaths().contains(path)) {
                processPlaceholderOrExpr(resource, path, (Map<String, Object>) v);
            }
            return v;
        }

        if (v instanceof List) {
            List<Object> list     = (List<Object>) v;
            String       itemPath = path + ".[]";
            for (int i = 0; i < list.size(); i++) {
                Object item  = list.get(i);
                Object value = processPlaceholderOrExpr(resource, itemPath, item);
                if (item != value) {
                    list.set(i, value);
                }
            }
            return v;
        }

        return v;
    }
}
