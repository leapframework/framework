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

import leap.core.AppConfig;
import leap.core.AppConfigContext;
import leap.core.AppConfigReader;
import leap.lang.Charsets;
import leap.lang.Locales;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.resource.Resource;

import java.util.Properties;

/**
 * Reads .properties file.
 */
public class PropsConfigReader implements AppConfigReader {

    @Override
    public boolean readBase(AppConfigContext context, Resource resource) {
        String filename = resource.getFilename();

        if(Strings.endsWithIgnoreCase(filename, ".properties") || Strings.endsWithIgnoreCase(filename,".properties.xml")) {
            readBase(context, Props.load(resource));
            return true;
        }

        return false;
    }

    @Override
    public boolean readFully(AppConfigContext context, Resource resource) {
        String filename = resource.getFilename();

        if(Strings.endsWithIgnoreCase(filename, ".properties") || Strings.endsWithIgnoreCase(filename,".properties.xml")) {
            readFully(context, Props.load(resource));
            return true;
        }

        return false;
    }

    protected void readBase(AppConfigContext context, Properties props) {
        String basePackage = props.getProperty(AppConfig.INIT_PROPERTY_BASE_PACKAGE);
        if(!Strings.isEmpty(basePackage)) {
            context.setBasePackage(basePackage);
        }

        String debugValue = props.getProperty(AppConfig.INIT_PROPERTY_DEBUG);
        if(!Strings.isEmpty(debugValue)){
            context.setDebug(Converts.toBoolean(debugValue));
        }

        String defaultLocaleString = props.getProperty(AppConfig.INIT_PROPERTY_DEFAULT_LOCALE);
        if(!Strings.isEmpty(defaultLocaleString)){
            context.setDefaultLocale(Locales.forName(defaultLocaleString));
        }

        String defaultEncodingName = props.getProperty(AppConfig.INIT_PROPERTY_DEFAULT_CHARSET);
        if(!Strings.isEmpty(defaultEncodingName)){
            context.setDefaultCharset(Charsets.forName(defaultEncodingName));
        }
    }

    protected void readFully(AppConfigContext context, Properties props) {
        props.forEach((k,v) -> context.getProperties().put((String)k, (String)v));
    }

}
