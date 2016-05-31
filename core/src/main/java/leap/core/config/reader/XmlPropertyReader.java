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
import leap.core.AppConfigException;
import leap.core.AppPropertyContext;
import leap.core.AppPropertyReader;
import leap.lang.Strings;
import leap.lang.io.IO;
import leap.lang.resource.Resource;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

public class XmlPropertyReader extends XmlConfigReaderBase implements AppPropertyReader {

    protected static final String DEFAULT_CHARSET_ELEMENT = "default-charset";

    @Override
    public boolean readProperties(AppPropertyContext context, Resource resource) {
        if(Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            doReadProperties(context, resource);
            return true;
        }
        return false;
    }

    protected void doReadProperties(AppPropertyContext context, Resource resource) {
        XmlReader reader = null;
        try{
            reader = XML.createReader(resource);

            boolean foundValidRootElement = false;

            while(reader.next()){
                if(reader.isStartElement(CONFIG_ELEMENT)){
                    foundValidRootElement = true;

                    Boolean defaultOverride = reader.getBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE);
                    if(null != defaultOverride) {
                        context.setDefaultOverride(defaultOverride);
                    }

                    doReadProperties(context,resource,reader);

                    if(null != defaultOverride) {
                        context.resetDefaultOverride();
                    }

                    break;
                }
            }

            if(!foundValidRootElement){
                throw new AppConfigException("No valid root element found in file : " + resource.getClasspath());
            }
        }finally{
            IO.close(reader);
        }
    }

    protected void doReadProperties(AppPropertyContext context, Resource resource, XmlReader reader) {
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(CONFIG_ELEMENT);
            return;
        }

        while(reader.nextWhileNotEnd(CONFIG_ELEMENT)){

            if(reader.isStartElement(BASE_PACKAGE_ELEMENT)){
                String basePackage = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(basePackage)) {
                    context.putProperty(resource,
                                        AppConfig.INIT_PROPERTY_BASE_PACKAGE,
                                        basePackage);
                }
                continue;
            }

            if(reader.isStartElement(DEBUG_ELEMENT)){
                String debug = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(debug)){
                    context.putProperty(resource,
                                        AppConfig.INIT_PROPERTY_DEBUG,
                                        debug);
                }
                continue;
            }

            if(reader.isStartElement(DEFAULT_LOCALE_ELEMENT)){
                String defaultLocale = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(defaultLocale)){
                    context.putProperty(resource,
                                        AppConfig.INIT_PROPERTY_DEFAULT_LOCALE,
                                        defaultLocale);
                }
                continue;
            }

            if(reader.isStartElement(DEFAULT_CHARSET_ELEMENT)){
                String defaultCharset = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(defaultCharset)){
                    context.putProperty(resource,
                                        AppConfig.INIT_PROPERTY_DEFAULT_CHARSET,
                                        defaultCharset);
                }
                continue;
            }

            if(reader.isStartElement(CONFIG_ELEMENT)){
                doReadProperties(context, resource, reader);
                reader.next();
                continue;
            }

            if(importResource(context, resource, reader)) {
                continue;
            }

            if(reader.isStartElement(PROPERTIES_ELEMENT)) {
                readProperties(context, resource, reader);
                continue;
            }

            if(reader.isStartElement(PROPERTY_ELEMENT)) {
                readProperty(context, resource, reader, "");
                continue;
            }
        }
    }

    protected void readProperties(AppPropertyContext context, Resource resource, XmlReader reader){
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(PROPERTIES_ELEMENT);
            return;
        }

        String prefix = reader.resolveAttribute(PREFIX_ATTRIBUTE);
        if(!Strings.isEmpty(prefix)) {
            char c = prefix.charAt(prefix.length() - 1);
            if(Character.isLetterOrDigit(c)) {
                prefix = prefix + ".";
            }
        }else{
            prefix = Strings.EMPTY;
        }

        while(reader.nextWhileNotEnd(PROPERTIES_ELEMENT)){
            if(reader.isStartElement(PROPERTY_ELEMENT)){
                readProperty(context, resource, reader, prefix);
                continue;
            }
        }
    }

    protected void readProperty(AppPropertyContext context, Resource resource, XmlReader reader, String prefix) {
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(PROPERTY_ELEMENT);
            return;
        }

        String  name     = reader.resolveRequiredAttribute(NAME_ATTRIBUTE);
        String  value    = reader.resolveAttribute(VALUE_ATTRIBUTE);
        boolean override = reader.resolveBooleanAttribute(OVERRIDE_ATTRIBUTE, context.isDefaultOverride());

        if(Strings.isEmpty(value)){
            value = reader.resolveElementTextAndEnd();
        }else{
            reader.nextToEndElement(PROPERTY_ELEMENT);
        }

        if(!override && context.hasProperty(name)){
            throw new AppConfigException("Found duplicated property '" + name + "' in resource : " + resource.getClasspath());
        }

        String key = prefix + name;
        context.putProperty(resource, key, value);
    }
}