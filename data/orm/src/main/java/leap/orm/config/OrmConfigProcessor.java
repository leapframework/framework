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

import leap.core.AppClassLoader;
import leap.core.AppConfigException;
import leap.core.config.AppConfigContext;
import leap.core.config.AppConfigProcessor;
import leap.lang.Strings;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.resource.Resources;
import leap.lang.xml.XmlReader;
import leap.orm.Orm;
import leap.orm.OrmConfig;
import leap.orm.OrmException;

public class OrmConfigProcessor implements AppConfigProcessor {

	public static final String NAMESPACE_URI = "http://www.leapframework.org/schema/orm/config";

    private static final String CONFIG                = "config";
    private static final String MODELS                = "models";
    private static final String PACKAGE               = "package";
    private static final String CLASS                 = "class";
    private static final String NAME                  = "name";
	private static final String TABLE                 = "table";
    private static final String DATASOURCE            = "datasource";
    private static final String SERIALIZER            = "serializer";
    private static final String DEFAULT_COLUMN_TYPE   = "default-column-type";
    private static final String DEFAULT_COLUMN_LENGTH = "default-column-length";

    @Override
    public String getNamespaceURI() {
	    return NAMESPACE_URI;
    }

	@Override
    public void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException {
		if(reader.isStartElement(CONFIG)){
			readConfig(context, reader);
			return;
		}
		
		if(reader.isStartElement(MODELS)) {
			readModels(context, reader);
			return;
		}

	}
	
	protected void readConfig(AppConfigContext context, XmlReader reader) throws AppConfigException {
        reader.forEachResolvedAttributes((n,v) -> {
            if(!Strings.isEmpty(v)) {
                String key = OrmConfig.KEY_PREFIX + "." + n.getLocalPart();
                context.putProperty(reader.getSource(), key, v);
            }
        });

        while(reader.nextWhileNotEnd(CONFIG)) {

            if(reader.isStartElement(SERIALIZER)) {
                readSerializer(context, reader);
                continue;
            }

        }
	}

    protected void readSerializer(AppConfigContext context, XmlReader reader) throws AppConfigException {
        OrmConfigExtension extension = context.getOrCreateExtension(OrmConfigExtension.class);

        String  name                = reader.getRequiredAttribute(NAME);
        String  defaultColumnType   = reader.getRequiredAttribute(DEFAULT_COLUMN_TYPE);
        Integer defaultColumnLength = reader.getIntegerAttribute(DEFAULT_COLUMN_LENGTH);

        SerializeConfigImpl sc = new SerializeConfigImpl();
        sc.setDefaultColumnType(JdbcTypes.forTypeName(defaultColumnType));
        sc.setDefaultColumnLength(defaultColumnLength);

        extension.addSerializeConfig(name, sc);
    }

	protected void readModels(AppConfigContext context, XmlReader reader) throws AppConfigException {
		OrmModelsConfigs mc = context.getExtension(OrmModelsConfigs.class);
		if(null == mc){
			mc = new OrmModelsConfigs();
			context.setExtension(mc);
		}
		
		String datasource = reader.resolveAttribute(DATASOURCE,Orm.DEFAULT_NAME);

		OrmModelsConfig models = mc.getModelsConfig(datasource, false);
		if(null == models) {
			models = new OrmModelsConfig();
			models.setDataSource(datasource);
			if(Strings.isEmpty(models.getDataSource())){
				mc.addModels(Orm.DEFAULT_NAME, models);
			}else {
				mc.addModels(models.getDataSource(), models);
			}

		}
		
		while(reader.nextWhileNotEnd(MODELS)) {
			if(reader.isStartElement(PACKAGE)) {
				String basePackage = reader.resolveAttribute(NAME);
				if(!Strings.isEmpty(basePackage)) {
					if(models.getBasePackages().containsKey(basePackage)){
						throw new AppConfigException("Duplicate orm package["+basePackage+"] config in " + reader.getSource() 
						+ " and " + models.getBasePackages().get(basePackage).getSource());
					}
					models.addBasePackage(new OrmModelPkgConfig(basePackage,reader.getSource()));
					context.addResources(Resources.scanPackage(basePackage));
                    AppClassLoader.addInstrumentPackage(basePackage);
				}
				continue;
			}
			
			if(reader.isStartElement(CLASS)) {
				String className = reader.resolveAttribute(NAME);
				String tableName = reader.resolveAttribute(TABLE);
				if(!Strings.isEmpty(className)) {
					if(models.getClasses().containsKey(className)){
						throw new AppConfigException("Duplicate orm class["+className+"] config in " + reader.getSource()
								+ " and " + models.getClasses().get(className).getSource());
					}
					models.addClassConfig(new OrmModelClassConfig(reader.getSource(), className,tableName));
                    AppClassLoader.addInstrumentClass(className);
				}
				continue;
			}
		}
	}
}