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

package leap.web.api.config;

import leap.lang.Strings;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.web.api.Apis;

public class XmlApiConfigReader implements ApiConfigReader {

    private static final Log log = LogFactory.get(XmlApiConfigReader.class);

    protected static final String APIS_ELEMENT      = "apis";
    protected static final String API_ELEMENT       = "api";
    protected static final String CONFIG_ELEMENT    = "config";
    protected static final String OAUTH_ELEMENT     = "oauth";

    protected static final String ENABLED_ATTR   = "enabled";
    protected static final String AUTHZ_URL_ATTR = "authz-url";
    protected static final String TOKEN_URL_ATTR = "token-url";
    protected static final String SCOPE_ATTR     = "scope";

    @Override
    public boolean readConfiguration(Apis apis, ApiConfigReaderContext context, Resource resource) {
        if(Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            readXml(apis, context, resource);
            return true;
        }
        return false;
    }

    protected void readXml(Apis apis, ApiConfigReaderContext context, Resource resource) {

        XmlReader reader = null;
        try{
            reader = XML.createReader(resource);
            reader.setPlaceholderResolver(context.getAppConfig().getPlaceholderResolver());

            boolean foundValidRootElement = false;

            while(reader.next()){
                if(reader.isStartElement(APIS_ELEMENT)){
                    foundValidRootElement = true;

                    readApi(apis, context, resource, reader);
                    break;
                }

                if(reader.isStartElement(API_ELEMENT)) {
                    foundValidRootElement = true;

                    readApi(apis, context, resource, reader);
                    break;
                }

                if(reader.isStartElement()) {
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

    protected void readApis(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {
        while(reader.nextToEndElement(APIS_ELEMENT)) {
            if(reader.isStartElement(CONFIG_ELEMENT)) {
                readConfig(apis, context, resource, reader);
                break;
            }

            if(reader.isStartElement(API_ELEMENT)) {
                readApi(apis, context, resource, reader);
                break;
            }
        }

    }

    protected void readApi(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {

        while(reader.nextToEndElement(API_ELEMENT)) {


        }

    }

    protected void readConfig(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {

        while(reader.nextToEndElement(CONFIG_ELEMENT)) {

            if(reader.isStartElement(OAUTH_ELEMENT)) {

                apis.setDefaultOAuthEnabled(
                        reader.resolveBooleanAttribute(ENABLED_ATTR, apis.isDefaultOAuthEnabled()));

                apis.setDefaultOAuthAuthorizationUrl(
                        reader.resolveAttribute(AUTHZ_URL_ATTR, apis.getDefaultOAuthAuthorizationUrl()));

                apis.setDefaultOAuthTokenUrl(
                        reader.resolveAttribute(TOKEN_URL_ATTR, apis.getDefaultOAuthTokenUrl()));

                continue;
            }

        }

    }

}