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

import leap.core.AppConfigContext;
import leap.core.AppConfigException;
import leap.core.AppConfigProcessor;
import leap.core.AppConfigReader;
import leap.core.ds.DataSourceConfig;
import leap.core.ds.DataSourceManager;
import leap.core.sys.SysPermission;
import leap.core.sys.SysPermissionDef;
import leap.lang.*;
import leap.lang.convert.Converts;
import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElClasses;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads .xml file.
 */
public class XmlConfigReader extends XmlConfigReaderBase implements AppConfigReader {

    private static final Log log = LogFactory.get(XmlConfigReader.class);

    private static final DefaultElParseContext parseContext = new DefaultElParseContext();
    static {
        parseContext.setFunction("classes:isPresent", ElClasses.createFunction(Classes.class, "isPresent(java.lang.String)"));
        parseContext.setFunction("strings:isEmpty",   ElClasses.createFunction(Strings.class, "isEmpty(java.lang.String)"));
    }

    private static final List<AppConfigProcessor> processors = Factory.newInstances(AppConfigProcessor.class);

    @Override
    public boolean readBase(AppConfigContext context, Resource resource) {
        if(Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            readBaseXml(context, resource);
            return true;
        }
        return false;
    }

    @Override
    public boolean readFully(AppConfigContext context, Resource resource) {
        if(Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            readFullXml(context, resource);
            return true;
        }
        return false;
    }

    protected void readBaseXml(AppConfigContext context, Resource resource) {
        XmlReader reader = null;
        try{
            reader = XML.createReader(resource);
            reader.setPlaceholderResolver(context.getPlaceholderResolver());

            boolean foundValidRootElement = false;

            while(reader.next()){
                if(reader.isStartElement(CONFIG_ELEMENT)){
                    foundValidRootElement = true;
                    readBaseProperties(context,resource,reader);
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

    protected void readFullXml(AppConfigContext context, Resource resource) {
        XmlReader reader = null;
        try{
            reader = XML.createReader(resource);
            reader.setPlaceholderResolver(context.getPlaceholderResolver());

            boolean foundValidRootElement = false;

            while(reader.next()){
                if(reader.isStartElement(CONFIG_ELEMENT)){
                    foundValidRootElement = true;

                    Boolean defaultOverride = reader.getBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE);
                    if(null != defaultOverride) {
                        context.setDefaultOverride(defaultOverride);
                    }

                    readConfig(context,resource,reader);

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

    private void readBaseProperties(AppConfigContext context, Resource resource, XmlReader reader) {
        while(reader.nextWhileNotEnd(CONFIG_ELEMENT)){

            if(reader.isStartElement(BASE_PACKAGE_ELEMENT)){
                context.setBasePackage(reader.resolveElementTextAndEnd());
                continue;
            }

            if(reader.isStartElement(DEBUG_ELEMENT)){
                String debugValue = reader.resolveElementTextAndEnd();
                if(!Strings.isEmpty(debugValue)){
                    context.setDebug(Converts.toBoolean(debugValue));
                }
                continue;
            }

            if(reader.isStartElement(DEFAULT_LOCALE_ELEMENT)){
                String defaultLocaleString = reader.resolveElementTextAndEnd();
                if(!Strings.isEmpty(defaultLocaleString)){
                     context.setDefaultLocale(Locales.forName(defaultLocaleString));
                }
                continue;
            }

            if(reader.isStartElement(DEFAULT_ENCODING_ELEMENT)){
                String defaultEncodingName = reader.resolveElementTextAndEnd();
                if(!Strings.isEmpty(defaultEncodingName)){
                    context.setDefaultCharset(Charsets.forName(defaultEncodingName));
                }
                continue;
            }
        }
    }

    private void readConfig(AppConfigContext context,Resource resource, XmlReader reader) {
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(CONFIG_ELEMENT);
            return;
        }

        while(reader.nextWhileNotEnd(CONFIG_ELEMENT)){

            //extension element
            if(reader.isStartElement() && !DEFAULT_NAMESPACE_URI.equals(reader.getElementName().getNamespaceURI())){
                processExtensionElement(context,reader);
                continue;
            }

            if(reader.isStartElement(CONFIG_ELEMENT)){
                readConfig(context, resource, reader);
                reader.next();
                continue;
            }

            if(reader.isStartElement(ADDITIONAL_PACKAGES_ELEMENT)) {
                String[] packages = Strings.splitMultiLines(reader.getElementTextAndEnd(), ',');
                if(packages.length > 0) {
                    Collections2.addAll(context.getAdditionalPackages(), packages);
                }
                continue;
            }

            if(reader.isStartElement(RESOURCES_ELEMENT)){
                if(matchProfile(context.getProfile(), reader)){
                    context.addResources(Resources.scan(reader.getRequiredAttribute(LOCATION_ATTRIBUTE)));
                }
                reader.nextToEndElement(RESOURCES_ELEMENT);
                continue;
            }

            if(importResource(context, resource, reader)) {
                continue;
            }

            /*
            if(reader.isStartElement(PROPERTIES_ELEMENT)){
                readProperties(context, resource, reader);
                continue;
            }

            if(reader.isStartElement(PROPERTY_ELEMENT)) {
                readProperty(context, resource, reader, "");
                continue;
            }
            */

            if(reader.isStartElement(DATASOURCE_ELEMENT)) {
                readDataSource(context, resource, reader);
                continue;
            }

            if(reader.isStartElement(PERMISSIONS_ELEMENT)){
                readPermissions(context, resource, reader);
                continue;
            }
        }
    }

    private void processExtensionElement(AppConfigContext context,XmlReader reader){
        String nsURI = reader.getElementName().getNamespaceURI();

        for(AppConfigProcessor extension : processors){

            if(nsURI.equals(extension.getNamespaceURI())){
                extension.processElement(context,reader);
                return;
            }

        }

        throw new AppConfigException("Namespace uri '" + nsURI + "' not supported, check your config : " + reader.getSource());
    }

    private void readDataSource(AppConfigContext context,Resource resource, XmlReader reader){
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(DATASOURCE_ELEMENT);
            return;
        }

        String dataSourceName = reader.getAttribute(NAME_ATTRIBUTE);
        if(Strings.isEmpty(dataSourceName)) {
            dataSourceName = DataSourceManager.DEFAULT_DATASOURCE_NAME;
        }

        if(context.hasDataSourceConfig(dataSourceName)) {
            throw new AppConfigException("Found duplicated datasource '" + dataSourceName + "', check your config : " + reader.getSource());
        }

        String dataSourceType = reader.getAttribute(TYPE_ATTRIBUTE);
        boolean isDefault     = reader.getBooleanAttribute(DEFAULT_ATTRIBUTE, false);

        DataSourceConfig.Builder conf = new DataSourceConfig.Builder();
        conf.setDataSourceType(dataSourceType);
        conf.setDefault(isDefault);

        if(isDefault && context.hasDefaultDataSourceConfig()) {
            throw new AppConfigException("Found duplicated default datasource'" + dataSourceName + "', check your config : " + reader.getSource());
        }

        while(reader.nextWhileNotEnd(DATASOURCE_ELEMENT)){

            if(reader.isStartElement(PROPERTY_ELEMENT)){
                String  name     = reader.resolveRequiredAttribute(NAME_ATTRIBUTE);
                String  value    = reader.resolveAttribute(VALUE_ATTRIBUTE);

                if(Strings.isEmpty(value)){
                    value = reader.resolveElementTextAndEnd();
                }else{
                    reader.nextToEndElement(PROPERTY_ELEMENT);
                }

                conf.setProperty(name, value);

                continue;
            }

            if(reader.isStartElement()) {
                String name  = reader.getElementLocalName();
                String value = Strings.trim(reader.resolveElementTextAndEnd());

                conf.setProperty(name, value);
                continue;
            }
        }

        context.setDataSourceConfig(dataSourceName, conf);
    }

    private void readProperties(AppConfigContext context,Resource resource, XmlReader reader){
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

    private void readProperty(AppConfigContext context, Resource resource, XmlReader reader, String prefix) {
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

        if(key.endsWith("[]")) {
            key = key.substring(0, key.length()-2);
            List<String> list = context.getArrayProperties().get(key);
            if(null == list) {
                list = new ArrayList<>();
                context.getArrayProperties().put(key, list);
            }
            list.add(value);
        }else{
            context.getProperties().put(key, value);
        }
    }

    private void readPermissions(AppConfigContext context,Resource resource,XmlReader reader){
        if(!matchProfile(context.getProfile(), reader)){
            reader.nextToEndElement(PERMISSIONS_ELEMENT);
            return;
        }

        while(reader.nextWhileNotEnd(PERMISSIONS_ELEMENT)){
            if(reader.isStartElement(GRANT_ELEMENT)){
                readPermission(context, resource, reader, true);
                continue;
            }

            if(reader.isStartElement(DENY_ELEMENT)){
                readPermission(context, resource, reader, false);
                continue;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readPermission(AppConfigContext context,Resource resource,XmlReader reader,boolean granted){
        String typeName  = reader.resolveAttribute(TYPE_ATTRIBUTE);
        String className = reader.resolveRequiredAttribute(CLASS_ATTRIBUTE);
        String name      = reader.resolveRequiredAttribute(NAME_ATTRIBUTE);
        String actions   = reader.resolveRequiredAttribute(ACTIONS_ATTRIBUTE);

        Class<?> permClass = Classes.tryForName(className);
        if(null == permClass){
            throw new AppConfigException("Permission class '" + className + "' not found, source : " + reader.getSource());
        }

        if(!SysPermission.class.isAssignableFrom(permClass)){
            throw new AppConfigException("Permission class '" + className + "' must be instanceof '" + SysPermission.class.getName() + "', source : " + reader.getSource());
        }

        Class<? extends SysPermission> permType = null;
        if(!Strings.isEmpty(typeName)){
            permType = (Class<? extends SysPermission>)Classes.tryForName(typeName);
            if(null == permType){
                throw new AppConfigException("Permission type class '" + typeName + "' not found, source : " + reader.getSource());
            }
        }else{
            permType = (Class<? extends SysPermission>)permClass;
        }

        try {
            Constructor<?> constructor = permClass.getConstructor(String.class,String.class);

            SysPermission permObject = (SysPermission)constructor.newInstance(name,actions);

            context.addPermission(new SysPermissionDef(reader.getSource(), permType, permObject, granted),context.isDefaultOverride());
        } catch (NoSuchMethodException e) {
            throw new AppConfigException("Permission class '" + className + "' must define the constructor(String.class,String.class), source : " + reader.getSource());
        } catch (Exception e){
            throw new AppConfigException("Error creating permission instance of class '" + className + ", source : " + reader.getSource(),e);
        }
    }
}
