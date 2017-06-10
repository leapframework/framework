/*
 * Copyright 2014 the original author or authors.
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
package leap.orm.sql;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.db.DbPlatforms;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.orm.OrmMetadata;
import leap.orm.metadata.SqlRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class XmlSqlReader implements SqlReader {
	
	private static final Log log = LogFactory.get(XmlSqlReader.class);
	
	private static final String SQLS_ELEMENT                = "sqls";
	private static final String IMPORT_ELEMENT              = "import";
	private static final String COMMAND_ELEMENT             = "command";
	private static final String FRAGMENT_ELEMENT			= "fragment";
	private static final String RESOURCE_ATTRIBUTE          = "resource";
	private static final String OVERRIDE_ATTRIBUTE          = "override";
	private static final String KEY_ATTRIBUTE               = "key";
	private static final String CHECK_EXISTENCE_ATTRIBUTE   = "check-existence";
	private static final String DEFAULT_OVERRIDE_ATTRIBUTE  = "default-override";
	private static final String LANG_ATTRIBUTE              = "lang";
    private static final String DATA_SOURCE                 = "data-source";
    private static final String WHERE_FIELDS                = "where-fields";
    private static final String QUERY_FILTER                = "query-filter";
	
	protected @Inject @M BeanFactory beanFactory;
	protected @Inject @M SqlLanguage defaultLanguage;
    protected @Inject @M SqlRegistry registry;

    protected Map<String, Map<String, DefaultSqlCommand>> loadedSqls = new HashMap<>();
	
	@Override
    public boolean readSqlCommands(SqlReaderContext context, Resource resource) throws IOException {
		if(Strings.endsWith(resource.getFilename(),".xml")){

            Map<String, DefaultSqlCommand> sqls = loadedSqls.get(resource.getURLString());
            if(null == sqls) {
                sqls = new HashMap<>();
                loadedSqls.put(resource.getURLString(), sqls);
                loadSqls(context,resource,context.isDefaultOverride(), sqls);
            }

            loadSqls(context, sqls);
			return true;
		}

	    return false;
    }

    protected void loadSqls(SqlReaderContext context, Map<String,DefaultSqlCommand> sqls) {

        final OrmMetadata md = context.getConfigContext().getMetadata();

        sqls.forEach((key,sql) -> {
            if(!matchContext(context, sql)) {
                return;
            }

            //Removes if exists.
            md.removeSqlCommand(key);

            //Clones a new one for current orm context.
            md.addSqlCommand(key, sql.clone());
        });
    }

    protected boolean matchContext(SqlReaderContext context, SqlCommand sql) {
        if(!Strings.isEmpty(sql.getDbType())) {

            final String currentDbType = context.getConfigContext().getDb().getType();
            if(!currentDbType.equalsIgnoreCase(sql.getDbType())) {
                return false;
            }

        }

        if(!Strings.isEmpty(sql.getDataSourceName())) {
            final String currentDataSourceName = context.getConfigContext().getName();

            if(!currentDataSourceName.equalsIgnoreCase(sql.getDataSourceName())) {
                return false;
            }
        }

        return true;
    }

    protected void loadSqls(SqlReaderContext context, Resource resource, boolean defaultOverride, Map<String,DefaultSqlCommand> sqls) throws IOException{
        try(XmlReader reader = XML.createReader(resource) ){
        	reader.setPlaceholderResolver(context.getConfigContext().getAppContext().getConfig().getPlaceholderResolver());	
        	loadSqls(context, resource, reader, defaultOverride, sqls);
        }
	}

	protected void loadSqls(SqlReaderContext context, Resource resource, XmlReader reader, boolean defaultOverride, Map<String,DefaultSqlCommand> sqls) throws IOException{
		boolean foundValidRootElement = false;
		
		String dbType = resolveDbType(resource);
        String defaultDataSource;

		while(reader.next()){
			if(reader.isStartElement(SQLS_ELEMENT)){
				foundValidRootElement = true;

                defaultDataSource = reader.getAttribute(DATA_SOURCE);
				
				while(reader.next()){
					if(reader.isStartElement(IMPORT_ELEMENT)){
						boolean checkExistence    = reader.resolveBooleanAttribute(CHECK_EXISTENCE_ATTRIBUTE, true);
						boolean override          = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE, defaultOverride);
						String importResourceName = reader.resolveRequiredAttribute(RESOURCE_ATTRIBUTE);
						
						Resource importResource = Resources.getResource(resource,importResourceName);
						
						if(null == importResource || !importResource.exists()){
							if(checkExistence){
								throw new SqlConfigException("the import resource '" + importResourceName + "' not exists");	
							}
						}else{
							loadSqls(context, importResource, override, sqls);
							reader.nextToEndElement(IMPORT_ELEMENT);
						}
						continue;
					}
					
					if(reader.isStartElement(COMMAND_ELEMENT)){
						readSqlCommand(context, reader, dbType, defaultDataSource, defaultOverride, sqls);
						continue;
					}

					if(reader.isStartElement(FRAGMENT_ELEMENT)){
						readSqlFragment(context, reader, dbType, defaultDataSource, defaultOverride);
						continue;
					}
				}
				break;
			}
		}
		
		if(!foundValidRootElement){
			throw new SqlConfigException("valid root element not found in file : " + resource.getClasspath());
		}
	}

	private void readSqlFragment(SqlReaderContext context, XmlReader reader, String dbType, String dataSource, boolean defaultOverride) {
		String key 	   = reader.resolveRequiredAttribute(KEY_ATTRIBUTE);
		String content = reader.resolveElementTextAndEnd();

		String fragmentDescription = "[ key =" + key + "]";
		if(Strings.isEmpty(content)){
			throw new SqlConfigException("The content body of sql fragment " + fragmentDescription + " must not be empty, xml : " + reader.getSource());
		}

		log.debug("Load sql fragment {} from [{}]",fragmentDescription,reader.getSource());
		registry.addSqlFragment(key,new SimpleSqlFragment(reader.getSource(),content));
	}

    protected boolean matchDataSource(SqlReaderContext context, XmlReader reader, String defaultDataSource) {
        String dataSource = reader.getAttribute(DATA_SOURCE);

        if(Strings.isEmpty(dataSource)) {
            dataSource = defaultDataSource;
        }

        if(Strings.isEmpty(dataSource)) {
            return true;
        }

        return context.getConfigContext().getName().equalsIgnoreCase(dataSource);
    }

	protected void readSqlCommand(SqlReaderContext context, XmlReader reader, String dbType, String defaultDataSource, boolean defaultOverride, Map<String,DefaultSqlCommand> sqls){
		String  key                 = reader.resolveRequiredAttribute(KEY_ATTRIBUTE);
		String  langName     	    = reader.resolveAttribute(LANG_ATTRIBUTE);
		boolean override            = reader.resolveBooleanAttribute(OVERRIDE_ATTRIBUTE, defaultOverride);
		String	content             = reader.getElementTextAndEnd();
		String  datasource			= reader.resolveAttribute(DATA_SOURCE, defaultDataSource);

        Boolean whereFields = reader.resolveBooleanAttribute(WHERE_FIELDS);
        Boolean queryFilter = reader.resolveBooleanAttribute(QUERY_FILTER);

		if(Strings.containsWhitespaces(key)) {
			throw new SqlConfigException("'key' attribute cannot contains whitespace characters [" + key + "], xml : " + reader.getSource());
		}
		
		log.debug("Load sql '{}' from [{}]", key, reader.getSource());
		
		if(Strings.isEmpty(content = Strings.trim(content))){
			throw new SqlConfigException("The content body of sql '" + key + "' must not be empty, xml : " + reader.getSource());
		}
		
		//check is sql command exists
		if(!Strings.isEmpty(key)){
			SqlCommand exists = registry.tryGetSqlCommand(key, dbType);
			if(null != exists){
				
				if(Strings.isEmpty(dbType) && !Strings.isEmpty(exists.getDbType())){
					return;
				}
				
				//override
				if(!Strings.isEmpty(dbType) && Strings.isEmpty(exists.getDbType())){
					override = true;
				}
				
				if(!override){
					throw new SqlConfigException("Found duplicated sql key '" + key + "', xmls : " + exists.getSource() + "," + reader.getSource());
				}
				
				registry.removeSqlCommand(key, dbType);
			}
		}

		SqlLanguage language;
		if(!Strings.isEmpty(langName)){
			language = beanFactory.tryGetBean(SqlLanguage.class,langName);
			if(null == language){
				throw new SqlConfigException("Sql language '" + langName + "' not found in sql '" + key + "', xml : " + reader.getSource());
			}
		}else{
			language = defaultLanguage;
		}
		
		log.trace("SQL(s) : \n\n  {}\n",content);
		DefaultSqlCommand command = new DefaultSqlCommand(reader.getSource(), key, dbType, language, content, datasource);
        command.setWhereFieldsEnabled(whereFields);
        command.setQueryFilterEnabled(queryFilter);

        registry.addSqlCommand(key, dbType, command);
        sqls.put(key, command);
	}
	
	protected String resolveDbType(Resource r) {
		String filename = r.getFilename();
		if(null != filename){
			String filenameWithoutExt = Paths.getFileNameWithoutExtension(filename);
			int lastIndexOfUnderscore = filenameWithoutExt.lastIndexOf('_');
			if(lastIndexOfUnderscore > 0){
				String dbType = filenameWithoutExt.substring(lastIndexOfUnderscore + 1);
				if(DbPlatforms.tryGet(dbType) != null){
					return dbType;
				}
			}
		}
		return null;
	}
}
