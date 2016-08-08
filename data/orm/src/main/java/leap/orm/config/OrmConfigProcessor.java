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
package leap.orm.config;

import leap.core.config.AppConfigContext;
import leap.core.AppConfigException;
import leap.core.config.AppConfigProcessor;
import leap.lang.Strings;
import leap.lang.resource.Resources;
import leap.lang.xml.XmlReader;
import leap.orm.Orm;
import leap.orm.OrmConfig;

public class OrmConfigProcessor implements AppConfigProcessor {

	public static final String NAMESPACE_URI = "http://www.leapframework.org/schema/orm/config";

	private static final String CONFIG_ELEMENT       		  = "config";
	private static final String MODELS_ELEMENT				  = "models";
	private static final String PACKAGE_ELEMENT			      = "package";
	private static final String CLASS_ELEMENT			      = "class";
    private static final String AUTO_GENERATE_COLUMNS         = "auto-generate-columns";
	private static final String DEFAULT_MAX_RESULTS_ATTRIBUTE = "default-max-results";
	private static final String NAME_ATTRIBUTE       		  = "name";
	private static final String DATASOURCE_ATTRIBUTE          = "datasource";
	
	@Override
    public String getNamespaceURI() {
	    return NAMESPACE_URI;
    }

	@Override
    public void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException {
		if(reader.isStartElement(CONFIG_ELEMENT)){
			processConfig(context, reader);
			return;
		}
		
		if(reader.isStartElement(MODELS_ELEMENT)) {
			processModels(context, reader);
			return;
		}

	}
	
	protected void processConfig(AppConfigContext context, XmlReader reader) throws AppConfigException {
        reader.forEachResolvedAttributes((n,v) -> {
            if(!Strings.isEmpty(v)) {
                String key = OrmConfig.KEY_PREFIX + "." + n.getLocalPart();
                context.putProperty(reader.getSource(), key, v);
            }
        });

        reader.nextToEndElement(CONFIG_ELEMENT);
	}
	
	protected void processModels(AppConfigContext context, XmlReader reader) throws AppConfigException {
		OrmModelsConfigs mc = context.getExtension(OrmModelsConfigs.class);
		if(null == mc){
			mc = new OrmModelsConfigs();
			context.setExtension(mc);
		}
		
		String datasource = reader.resolveAttribute(DATASOURCE_ATTRIBUTE, Orm.DEFAULT_NAME);
		
		OrmModelsConfig models = mc.getModelsConfig(datasource);
		if(null == models) {
			models = new OrmModelsConfig();
			mc.addModels(datasource, models);
		}
		
		while(reader.nextWhileNotEnd(MODELS_ELEMENT)) {
			if(reader.isStartElement(PACKAGE_ELEMENT)) {
				String basePackage = reader.resolveAttribute(NAME_ATTRIBUTE);
				if(!Strings.isEmpty(basePackage)) {
                    if(!models.removeBasePackage(basePackage)) {
						context.addResources(Resources.scanPackage(basePackage));
					}
					models.addBasePackage(basePackage);
				}
				continue;
			}
			
			if(reader.isStartElement(CLASS_ELEMENT)) {
				String className = reader.resolveAttribute(NAME_ATTRIBUTE);
				if(!Strings.isEmpty(className)) {
                    models.removeClassName(className);
					models.addClassName(className);
				}
				continue;
			}
		}
	}
}