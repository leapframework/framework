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

/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.config.reader;

import leap.core.config.AppProperty;
import leap.core.config.AppPropertyContext;
import leap.core.config.AppPropertyReader;
import leap.core.config.ConfigUtils;
import leap.lang.Strings;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.yaml.YAML;

import java.util.Map;

public class YamlPropertyReader implements AppPropertyReader {

    private static final leap.lang.logging.Log log = LogFactory.get(YamlPropertyReader.class);

    @Override
    public boolean readProperties(AppPropertyContext context, Resource resource) {
        String filename = resource.getFilename();

        if(Strings.endsWithIgnoreCase(filename, ".yaml") || Strings.endsWithIgnoreCase(filename,".yml")) {
            String content = resource.getContent();
            if(Strings.isEmpty(content)) {
                return true;
            }


            Map<String, Object> map = YAML.parse(content).asMap();
            if(map.isEmpty()) {
                return true;
            }

            Map<String, String> props = ConfigUtils.toProperties(map);
            props.forEach((key, value) -> putProperty(context, resource, key, value));
            return true;
        }

        return false;
    }

    protected void putProperty(AppPropertyContext context, Resource resource, String key, String value) {
        AppProperty p = context.getProperty(key);
        if(null != p) {
            log.info("Override property '{}' at '{}' by '{}'", key, p.getSource(), resource.getDescription());
        }
        context.putProperty(resource, key ,value);
    }

}