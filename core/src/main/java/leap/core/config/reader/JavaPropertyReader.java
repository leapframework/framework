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

import leap.core.AppConfigException;
import leap.core.config.AppPropertyContext;
import leap.core.config.AppPropertyReader;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.resource.Resource;

public class JavaPropertyReader implements AppPropertyReader {

    @Override
    public boolean readProperties(AppPropertyContext context, Resource resource) {
        String filename = resource.getFilename();

        if(Strings.endsWithIgnoreCase(filename, ".properties") ||
           Strings.endsWithIgnoreCase(filename,".properties.xml")) {

            Props.load(resource).forEach(
                (key, value) -> putProperty(context, resource, (String)key, (String)value)
            );

            return true;
        }

        return false;
    }

    protected void checkDuplicateProperty(AppPropertyContext context, Resource resource, String name) {
        if(!context.isDefaultOverride() && context.hasProperty(name)) {
            throw new AppConfigException("Found duplicated property '" + name + "', check config : " + resource);
        }
    }

    protected void putProperty(AppPropertyContext context, Resource resource, String key, String value) {
        checkDuplicateProperty(context, resource, key);
        context.putProperty(resource, key ,value);
    }

}