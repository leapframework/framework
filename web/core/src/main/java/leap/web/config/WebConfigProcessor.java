/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.config;

import java.util.Iterator;

import leap.core.AppConfigContext;
import leap.core.AppConfigException;
import leap.core.AppConfigProcessor;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.annotation.Internal;
import leap.lang.path.Paths;
import leap.lang.xml.XmlReader;
import leap.web.assets.AssetConfigExtension;
import leap.web.cors.CorsConfig;
import leap.web.error.ErrorsConfig;

@Internal
public class WebConfigProcessor implements AppConfigProcessor {

	public static final String NAMESPACE_URI = "http://www.leapframework.org/schema/web/config";

    //mvc
	private static final String MVC_ELEMENT                      = "mvc";
	private static final String DEFAULT_THEME_ATTRIBUTE          = "default-theme";
	private static final String THEMES_LOCATION_ATTRIBUTE        = "themes-location";
	private static final String VIEWS_LOCATION_ATTRIBUTE         = "views-location";
	private static final String DEFAULT_FORMAT_ATTRIBUTE         = "default-format";
	private static final String FORMAT_PARAMETER_ATTRIBUTE       = "format-parameter";
	private static final String TRIM_PARAMETERS_ATTRIBUTE        = "trim-parameters";
	private static final String ALLOW_ACTION_EXTENSION_ATTRIBUTE = "allow-action-extension";
	private static final String ALLOW_FORMAT_EXTENSION_ATTRIBUTE = "allow-format-extension";
	private static final String ALLOW_FORMAT_PARAMETER_ATTRIBUTE = "allow-format-parameter";
	private static final String ACTION_EXTENSIONS_ATTRIBUTE      = "action-extensions";
    private static final String CORS_ENABLED                     = "cors-enabled";

    //errors
	private static final String ERRORS_ELEMENT                   = "errors";
	private static final String ERROR_VIEW_ELEMENT               = "error-view";
	private static final String ERROR_CODE_ELEMENT               = "error-code";
	private static final String ERROR_CODE_ATTRIBUTE             = "error-code";
	private static final String EXCEPTION_TYPE_ATTRIBUTE         = "exception-type";
	private static final String VIEW_PATH_ATTRIBUTE              = "view-path";

    //cors
    private static final String CORS_ELEMENT                     = "cors";

    //assets
    private static final String ASSETS_ELEMENT                   = "assets";
    private static final String FOLDER_ELEMENT                   = "folder";
    private static final String LOCATION_ATTRIBUTE               = "location";
    private static final String PATH_PREFIX_ATTRIBUTE            = "path-prefix";

    @Override
    public String getNamespaceURI() {
	    return NAMESPACE_URI;
    }

	@Override
    public void processElement(AppConfigContext context, XmlReader reader) {
		if(reader.isStartElement(MVC_ELEMENT)){
            readMvcConfig(context, reader);
            return;
		}

        if(reader.isStartElement(ERRORS_ELEMENT)) {
            readErrorsConfig(context, reader);
            return;
        }

        if(reader.isStartElement(ASSETS_ELEMENT)) {
            readAssetsConfig(context, reader);
            return;
        }
		
		throw new AppConfigException("Unknown xml element '" + reader.getElementLocalName() + "', check the config : " + reader.getSource());
    }

	protected void readMvcConfig(AppConfigContext context, XmlReader reader) {
        //theme name
        String themeName = reader.getAttribute(DEFAULT_THEME_ATTRIBUTE);
        if(!Strings.isEmpty(themeName)){
            context.setProperty(WebConfigurator.CONFIG_DEFAULT_THEME, themeName);
        }

        String themesLocation = reader.getAttribute(THEMES_LOCATION_ATTRIBUTE);
        if(!Strings.isEmpty(themesLocation)){
            context.setProperty(WebConfigurator.CONFIG_THEMES_LOCATION, themesLocation);
        }

        //views-location
        String viewsLocation = reader.getAttribute(VIEWS_LOCATION_ATTRIBUTE);
        if(!Strings.isEmpty(viewsLocation)){
            if(!(viewsLocation.startsWith("/"))){
                throw new AppConfigException("views-location must starts with '/', check the config : " + reader.getSource());
            }
            if(viewsLocation.endsWith("/")){
                viewsLocation = viewsLocation.substring(0,viewsLocation.length() - 2);
            }
            context.setProperty(WebConfigurator.CONFIG_VIEWS_LOCATION, viewsLocation);
        }

        //default-format
        String defaultFormat = reader.getAttribute(DEFAULT_FORMAT_ATTRIBUTE);
        if(!Strings.isEmpty(defaultFormat)){
            context.setProperty(WebConfigurator.CONFIG_DEFAULT_FORMAT, defaultFormat);
        }

        //format-parameter
        String formatParameter = reader.getAttribute(FORMAT_PARAMETER_ATTRIBUTE);
        if(!Strings.isEmpty(formatParameter)){
            context.setProperty(WebConfigurator.CONFIG_FORMAT_PARAMETER, formatParameter);
        }

        //allow-format-extension
        Boolean allowFormatExtension = reader.getBooleanAttribute(ALLOW_FORMAT_EXTENSION_ATTRIBUTE);
        if(null != allowFormatExtension){
            context.setProperty(WebConfigurator.CONFIG_ALLOW_FORMAT_EXTENSION,String.valueOf(allowFormatExtension));
        }

        //allow-format-parameter
        Boolean allowFormatParameter = reader.getBooleanAttribute(ALLOW_FORMAT_PARAMETER_ATTRIBUTE);
        if(null != allowFormatParameter){
            context.setProperty(WebConfigurator.CONFIG_ALLOW_FORMAT_PARAMETER, String.valueOf(allowFormatParameter));
        }

        //trim-parameters
        Boolean trimParameters = reader.getBooleanAttribute(TRIM_PARAMETERS_ATTRIBUTE);
        if(null != trimParameters){
            context.setProperty(WebConfigurator.CONFIG_TRIM_PARAMETERS,String.valueOf(trimParameters));
        }

        //allow-action-extension
        Boolean allowActionExtension = reader.getBooleanAttribute(ALLOW_ACTION_EXTENSION_ATTRIBUTE);
        if(null != allowActionExtension){
            context.setProperty(WebConfigurator.CONFIG_ALLOW_ACTION_EXTENSION, String.valueOf(allowActionExtension));
        }

        //action-extensions
        String actionExtensions = reader.getAttribute(ACTION_EXTENSIONS_ATTRIBUTE);
        if(!Strings.isEmpty(actionExtensions)){
            context.setProperty(WebConfigurator.CONFIG_ACTION_EXTENSIONS, actionExtensions);
        }

        //cors-enabled
        String corsEnabled = reader.getAttribute(CORS_ENABLED);
        if(!Strings.isEmpty(corsEnabled)){
            context.setProperty(WebConfigurator.CONFIG_CORS_ENABLED, corsEnabled);
        }

        reader.next();
        while(!reader.isEndElement(MVC_ELEMENT)){
            readMvcChilds(context, reader);
        }
    }
	
	protected void readMvcChilds(AppConfigContext context, XmlReader reader){
		if(reader.isStartElement(ERRORS_ELEMENT)){
			readErrorsConfig(context, reader);
		}else if(reader.isStartElement(CORS_ELEMENT)) {
			readCorsConfig(context, reader);
		}else{
			reader.next();
		}
	}
	
	protected void readErrorsConfig(AppConfigContext context, XmlReader reader) {
		ErrorsConfig ec = context.getExtension(ErrorsConfig.class);
		if(null == ec) {
			ec = new ErrorsConfig();
			context.setExtension(ErrorsConfig.class,ec);
		}
		
		while(true){
			reader.next();
			
			if(reader.isStartElement(ERROR_VIEW_ELEMENT)){
				Integer code     = reader.getIntegerAttribute(ERROR_CODE_ATTRIBUTE);
				String  typeName = reader.getAttribute(EXCEPTION_TYPE_ATTRIBUTE);
				String  view     = reader.getAttributeRequired(VIEW_PATH_ATTRIBUTE);
				
				if(null != code){
					ec.addErrorView(code, view);
				}else if(!Strings.isEmpty(typeName)){
					Class<?> exceptionType = Classes.tryForName(typeName);
					if(null == exceptionType){
						throw new AppConfigException("Invalid exception class '" + typeName + "' in xml '" + reader.getSource() + "'");
					}
					ec.addErrorView(exceptionType, view);
				}else{
					throw new AppConfigException("Either 'error-code' or 'exception-type' attribute must not be empty in 'error-view' element, xml '" + reader.getSource() + "'");
				}
				
				reader.nextToEndElement(ERROR_VIEW_ELEMENT);
			}
			
			if(reader.isStartElement(ERROR_CODE_ELEMENT)) {
				String code     = reader.getAttributeRequired(ERROR_CODE_ATTRIBUTE);
				String typeName = reader.getAttributeRequired(EXCEPTION_TYPE_ATTRIBUTE);
				
				Class<?> exceptionType = Classes.tryForName(typeName);
				if(null == exceptionType) {
					throw new AppConfigException("Invalid exception class '" + typeName + "' in xml '" + reader.getSource() + "'");
				}
				
				ec.addErrorCode(exceptionType, code);
				reader.nextToEndElement(ERROR_CODE_ELEMENT);
			}
			
			if(reader.isEndElement(ERRORS_ELEMENT)){
				break;
			}
		}
	}
	
	/*
	 * <pre>
	 * 
	 * <cors allowed-origins="*" allowed-methods="" allowed-headers="" exposed-headers=""/>
	 * 
	 * </pre>
	 */
	protected void readCorsConfig(AppConfigContext context, XmlReader reader) {
		Iterator<String> attrs = reader.getAttributeNames();
		if(attrs.hasNext()){
			do{
				String name  = attrs.next();
				String value = reader.resolveAttribute(name);
				
				context.setProperty(CorsConfig.CONFIX_PREFIX + "." + name, value);
			}while(attrs.hasNext());
		}
	}

    protected void readAssetsConfig(AppConfigContext context, XmlReader reader) {
        AssetConfigExtension config = context.getOrCreateExtension(AssetConfigExtension.class);

        while(reader.nextWhileNotEnd(ASSETS_ELEMENT)) {

            if(reader.isStartElement(FOLDER_ELEMENT)) {
                String location   = reader.resolveRequiredAttribute(LOCATION_ATTRIBUTE);
                String pathPrefix = reader.resolveAttribute(PATH_PREFIX_ATTRIBUTE);

                config.addFolder(new AssetConfigExtension.AssetFolderConfig(location, pathPrefix));
                continue;
            }

        }
    }
}