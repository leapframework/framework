/*
 *  Copyright 2020 the original author or authors.
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
 *
 */

package app.beans;

import leap.core.AppConfigException;
import leap.core.config.AppConfigContext;
import leap.core.config.AppConfigProcessor;
import leap.lang.xml.XmlReader;

import java.util.HashMap;
import java.util.Map;

public class TestAppConfigProcessor implements AppConfigProcessor {

    public static final Map<String, String> properties = new HashMap<>();

    public static Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getNamespaceURI() {
        return "http://www.leapframework.org/schema/test/config";
    }

    @Override
    public void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException {
        reader.getAttributeLocalNames().forEachRemaining(name -> {
            properties.put(name, reader.resolveAttribute(name));
        });
    }
}
