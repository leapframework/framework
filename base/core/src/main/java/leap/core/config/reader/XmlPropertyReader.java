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
import leap.core.config.AppPropertyContext;
import leap.core.config.AppPropertyLoaderConfig;
import leap.core.config.AppPropertyReader;
import leap.core.el.EL;
import leap.lang.New;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.el.spel.SPEL;
import leap.lang.expression.Expression;
import leap.lang.extension.ExProperties;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.LogUtils;
import leap.lang.resource.Resource;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class XmlPropertyReader extends XmlConfigReaderBase implements AppPropertyReader {

    private static final Log log = LogFactory.get(XmlPropertyReader.class);

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

                if(reader.isStartElement(PROPERTIES_ELEMENT)) {
                    foundValidRootElement = true;

                    readProperties(context, resource, reader);

                    break;
                }
            }

            if(!foundValidRootElement){
                log.info("No valid root element found in file : " + resource.getClasspath());
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

            if(reader.isStartElement(LAZY_TEMPLATE_ELEMENT)){
                String lazy = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(lazy)){
                    context.putProperty(resource,
                            AppConfig.INIT_PROPERTY_LAZY_TEMPLATE,
                            lazy);
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

            if(reader.isStartElement(PROPERTIES_ELEMENT)) {
                readProperties(context, resource, reader);
                continue;
            }

            if(reader.isStartElement(PROPERTY_ELEMENT)) {
                readProperty(context, resource, reader, "");
                continue;
            }

            if(importResource(context, resource, reader)) {
                continue;
            }

            if(reader.isStartElement(CONFIG_LOADER_ELEMENT)) {
                readLoader(context, resource, reader, null);
                continue;
            }

            if(reader.isStartElement(IF_ELEMENT)) {
                Function<Map<String,String>, Boolean> ifExpr = createIfFunction(reader);

                while(reader.nextWhileNotEnd(IF_ELEMENT)) {

                    if(reader.isStartElement(CONFIG_LOADER_ELEMENT)) {
                        readLoader(context, resource, reader, ifExpr);
                        continue;
                    }
                }
                continue;
            }
        }
    }

    protected void readProperties(AppPropertyContext context, Resource resource, XmlReader reader){
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(PROPERTIES_ELEMENT);
            return;
        }

        boolean override = reader.resolveBooleanAttribute(OVERRIDE_ATTRIBUTE,context.isDefaultOverride());
        context.setDefaultOverride(override);

        String prefix = reader.resolveAttribute(PREFIX_ATTRIBUTE);
        if(!Strings.isEmpty(prefix)) {
            char c = prefix.charAt(prefix.length() - 1);
            if(Character.isLetterOrDigit(c)) {
                prefix = prefix + ".";
            }
        }else{
            prefix = Strings.EMPTY;
        }

        StringBuilder chars      = new StringBuilder();
        boolean       hasElement = false;
        while(reader.nextWhileNotEnd(PROPERTIES_ELEMENT)){
            if(reader.isCharacters()) {
                chars.append(reader.getCharacters());
                continue;
            }

            if(reader.isStartElement(PROPERTY_ELEMENT)){
                hasElement = true;
                readProperty(context, resource, reader, prefix);
                continue;
            }

            if(reader.isStartElement()) {
                hasElement = true;

                String name  = reader.getElementLocalName();
                String value = reader.getElementTextAndEnd();

                String key = prefix + name;
                putProperty(context, resource, key, value, context.isDefaultOverride());
                continue;
            }
        }

        if(!hasElement) {
            String text = chars.toString().trim();
            if(text.length() > 0) {
                ExProperties props = Props.loadKeyValues(text);

                final String finalPrefix = prefix;
                props.forEach((k,v) -> {
                    String key   = finalPrefix + k;
                    String value = (String)v;

                    int sharpIndex = value.indexOf('#');
                    if(sharpIndex > 0) {
                        value = value.substring(0,sharpIndex).trim();
                    }

                    putProperty(context, resource, key, value, context.isDefaultOverride());
                });
            }
        }
        context.resetDefaultOverride();
    }

    protected void putProperty(AppPropertyContext context, Resource resource, String key, String value, boolean override) {
        if(!override && context.hasProperty(key)) {
            throw new AppConfigException("Found duplicate property '" + key + "' in resource : " + LogUtils.getUrl(resource));
        }
        context.putProperty(resource, key, value);
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

        String key = prefix + name;
        putProperty(context, resource, key, value, override);
    }

    protected void readLoader(AppPropertyContext context, Resource resource, XmlReader reader,
                              Function<Map<String,String>, Boolean> enabled){
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(CONFIG_LOADER_ELEMENT);
            return;
        }

        String className    = reader.resolveRequiredAttribute(CLASS_ATTRIBUTE);
        float  sortOrder    = reader.resolveFloatAttribute(SORT_ORDER_ATTRIBUTE, 100);

        LoaderConfig loader = new LoaderConfig(className, enabled, sortOrder);

        while(reader.nextWhileNotEnd(CONFIG_LOADER_ELEMENT)){
            if(reader.isStartElement(PROPERTY_ELEMENT)){
                String  name  = reader.getAttribute(NAME_ATTRIBUTE);
                String  value = reader.getAttribute(VALUE_ATTRIBUTE);

                if(Strings.isEmpty(value)) {
                    value = reader.getElementTextAndEnd();
                }else{
                    reader.nextToEndElement(PROPERTY_ELEMENT);
                }

                loader.getProperties().put(name, value);

                continue;
            }
        }

        context.addLoader(loader);
    }

    protected Function<Map<String,String>, Boolean> createIfFunction(XmlReader reader) {
        String expr = reader.getRequiredAttribute(EXPR_ATTRIBUTE);

        Expression expression = SPEL.createExpression(parseContext, expr);
        return (props) -> {
            Map<String,Object> vars = New.hashMap("properties", props);
            return EL.test(expression.getValue(vars), true);
        };
    }

    protected static class LoaderConfig implements AppPropertyLoaderConfig {

        private final Function<Map<String,String>, Boolean> enabled;
        private final float                                 order;
        private final String                                className;
        private final Map<String, String>                   properties = new LinkedHashMap<>();

        public LoaderConfig(String className, Function<Map<String,String>, Boolean> enabled, float order) {
            this.className = className;
            this.enabled   = enabled;
            this.order     = order;
        }

        @Override
        public boolean load(Map<String, String> properties) {
            return null == enabled ? true : enabled.apply(properties);
        }

        @Override
        public float getSortOrder() {
            return order;
        }

        public String getClassName() {
            return className;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }
}