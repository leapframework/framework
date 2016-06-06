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
import leap.core.config.AppConfigContext;
import leap.core.config.AppConfigProcessor;
import leap.core.config.AppConfigReader;
import leap.core.el.EL;
import leap.core.monitor.DefaultMonitorConfig;
import leap.core.monitor.MonitorConfig;
import leap.core.sys.SysPermission;
import leap.core.sys.SysPermissionDef;
import leap.lang.*;
import leap.lang.el.spel.SPEL;
import leap.lang.expression.Expression;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Reads .xml file.
 */
public class XmlConfigReader extends XmlConfigReaderBase implements AppConfigReader {

    private static final Log log = LogFactory.get(XmlConfigReader.class);

    private static final List<AppConfigProcessor> processors = Factory.newInstances(AppConfigProcessor.class);

    @Override
    public boolean readConfig(AppConfigContext context, Resource resource) {
        if(Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            readFullXml(context, resource);
            return true;
        }
        return false;
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

                if(reader.isStartElement(PROPERTIES_ELEMENT)) {
                    //ignore the properties config file.
                    return;
                }
            }

            if(!foundValidRootElement){
                throw new AppConfigException("No valid root element found in file : " + resource.getClasspath());
            }
        }finally{
            IO.close(reader);
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
            if(reader.isStartElement(DATASOURCE_ELEMENT)) {
                readDataSource(context, resource, reader);
                continue;
            }
            */

            if(reader.isStartElement(PERMISSIONS_ELEMENT)){
                readPermissions(context, resource, reader);
                continue;
            }

            if(reader.isStartElement(MONITOR_ELEMENT)) {
                readMonitor(context, resource, reader);
                continue;
            }

            if(reader.isStartElement(IF_ELEMENT)) {

                if(testIfElement(context, reader)) {

                    while(reader.nextWhileNotEnd(IF_ELEMENT)) {

                        if(reader.isStartElement(MONITOR_ELEMENT)) {
                            readMonitor(context, resource, reader);
                            continue;
                        }

                        if(reader.isStartElement(PERMISSIONS_ELEMENT)) {
                            readPermissions(context, resource, reader);
                            continue;
                        }

                    }

                }else{
                    reader.nextToEndElement(IF_ELEMENT);
                }

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

    /*
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
    */

    private void readMonitor(AppConfigContext context, Resource resource, XmlReader reader) {
        DefaultMonitorConfig config = context.getOrCreateExtension(MonitorConfig.class, DefaultMonitorConfig.class);

        Boolean enabled     = reader.getBooleanAttribute(ENABLED_ATTRIBUTE);
        Boolean reportError = reader.getBooleanAttribute(REPORT_ERROR_ATTRIBUTE);
        Integer threshold   = reader.getIntegerAttribute(METHOD_THRESHOLD_ATTRIBUTE);

        if(null != enabled) {
            config.setEnabled(enabled);
        }

        if(null != reportError) {
            config.setReportError(reportError);
        }

        if(null != threshold) {
            config.setMethodThreshold(threshold);
        }

        reader.nextToEndElement();
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

    private boolean testIfElement(AppConfigContext context, XmlReader reader) {
        String expressionText = reader.getRequiredAttribute(EXPR_ATTRIBUTE);
        try {
            Expression expression = SPEL.createExpression(parseContext,expressionText);
            Map<String,Object> vars = New.hashMap("properties", context.getProperties());

            return EL.test(expression.getValue(vars), true);
        } catch (Exception e) {
            throw new AppConfigException("Error testing if expression '" + expressionText + "' at " + reader.getCurrentLocation(), e);
        }
    }
}
