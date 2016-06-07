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

import leap.core.config.AppConfigContextBase;
import leap.core.AppConfigException;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElClasses;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XmlReader;

public abstract class XmlConfigReaderBase {

    protected static final DefaultElParseContext parseContext = new DefaultElParseContext();
    static {
        parseContext.setFunction("classes:isPresent", ElClasses.createFunction(Classes.class, "isPresent(java.lang.String)"));
        parseContext.setFunction("strings:isEmpty",   ElClasses.createFunction(Strings.class, "isEmpty(java.lang.String)"));
    }

    public static final String DEFAULT_NAMESPACE_URI = "http://www.leapframework.org/schema/config";

    protected static final String CONFIG_ELEMENT               = "config";
    protected static final String BASE_PACKAGE_ELEMENT         = "base-package";
    protected static final String ADDITIONAL_PACKAGES_ELEMENT  = "additional-packages";
    protected static final String DEBUG_ELEMENT                = "debug";
    protected static final String DEFAULT_LOCALE_ELEMENT       = "default-locale";
    protected static final String DEFAULT_CHARSET_ELEMENT      = "default-charset";
    protected static final String IF_ELEMENT                   = "if";
    protected static final String CONFIG_LOADER_ELEMENT        = "loader";
    protected static final String DATASOURCE_ELEMENT           = "datasource";
    protected static final String IMPORT_ELEMENT               = "import";
    protected static final String PROPERTIES_ELEMENT           = "properties";
    protected static final String PROPERTY_ELEMENT             = "property";
    protected static final String PERMISSIONS_ELEMENT          = "permissions";
    protected static final String GRANT_ELEMENT                = "grant";
    protected static final String DENY_ELEMENT                 = "deny";
    protected static final String RESOURCES_ELEMENT            = "resources";
    protected static final String MONITOR_ELEMENT              = "monitor";
    protected static final String RESOURCE_ATTRIBUTE           = "resource";
    protected static final String IF_PROFILE_ATTRIBUTE         = "if-profile";
    protected static final String OVERRIDE_ATTRIBUTE           = "override";
    protected static final String PREFIX_ATTRIBUTE             = "prefix";
    protected static final String TYPE_ATTRIBUTE               = "type";
    protected static final String DEFAULT_ATTRIBUTE            = "default";
    protected static final String CLASS_ATTRIBUTE              = "class";
    protected static final String ACTIONS_ATTRIBUTE            = "actions";
    protected static final String CHECK_EXISTENCE_ATTRIBUTE    = "check-existence";
    protected static final String DEFAULT_OVERRIDE_ATTRIBUTE   = "default-override";
    protected static final String NAME_ATTRIBUTE               = "name";
    protected static final String VALUE_ATTRIBUTE              = "value";
    protected static final String LOCATION_ATTRIBUTE           = "location";
    protected static final String IF_ATTRIBUTE                 = "if";
    protected static final String SORT_ORDER_ATTRIBUTE         = "sort-order";
    protected static final String ENABLED_ATTRIBUTE            = "enabled";
    protected static final String METHOD_THRESHOLD_ATTRIBUTE   = "method-threshold";
    protected static final String EXPR_ATTRIBUTE               = "expr";
    protected static final String REPORT_ERROR_ATTRIBUTE       = "report-error";
    protected static final String REPORT_ARGS_ATTRIBUTE        = "report-args";
    protected static final String REPORT_LINE_NUMBER_ATTRIBUTE = "report-line-number";

    protected boolean importResource(AppConfigContextBase context, Resource parent, XmlReader reader) {
        if(reader.isStartElement(IMPORT_ELEMENT)){
            if(matchProfile(context.getProfile(), reader)){
                boolean checkExistence    = reader.resolveBooleanAttribute(CHECK_EXISTENCE_ATTRIBUTE, true);
                boolean override          = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE,context.isDefaultOverride());
                String importResourceName = reader.resolveRequiredAttribute(RESOURCE_ATTRIBUTE);

                Resource importResource = Resources.getResource(parent,importResourceName);

                if(null == importResource || !importResource.exists()){
                    if(checkExistence){
                        throw new AppConfigException("The import resource '" + importResourceName + "' not exists");
                    }
                }else{
                    context.importResource(importResource, override);
                }
            }
            reader.nextToEndElement(IMPORT_ELEMENT);
            return true;
        }

        return false;
    }

    protected boolean matchProfile(String profile, XmlReader element) {
        String profileName = element.getAttribute(IF_PROFILE_ATTRIBUTE);
        if(!Strings.isEmpty(profileName)){
            return Strings.equalsIgnoreCase(profile, profileName);
        }else{
            return true;
        }
    }
}
