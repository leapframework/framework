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
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.orm.OrmMetadata;
import leap.orm.mapping.EntityMapping;

import java.io.IOException;

public class XmlSqlReader implements SqlReader {
	
	private static final Log log = LogFactory.get(XmlSqlReader.class);
	
	private static final String SQLS_ELEMENT                = "sqls";
	private static final String IMPORT_ELEMENT              = "import";
	private static final String COMMAND_ELEMENT             = "command";
	private static final String FRAGMENT_ELEMENT			= "fragment";
	private static final String RESOURCE_ATTRIBUTE          = "resource";
	private static final String OVERRIDE_ATTRIBUTE          = "override";
	private static final String KEY_ATTRIBUTE               = "key";
	private static final String ENTITY_CLASS_ATTRIBUTE      = "entity-class";
	private static final String ENTITY_NAME_ATTRIBUTE       = "entity-name";
	private static final String CHECK_EXISTENCE_ATTRIBUTE   = "check-existence";
	private static final String DEFAULT_OVERRIDE_ATTRIBUTE  = "default-override";
	private static final String NAME_ATTRIBUTE              = "name";
	private static final String LANG_ATTRIBUTE              = "lang";
	
	protected @Inject @M BeanFactory beanFactory;
	protected @Inject @M SqlLanguage defaultLanguage;
	
	@Override
    public boolean readSqlCommands(SqlReaderContext context, Resource resource) throws IOException {
		if(Strings.endsWith(resource.getFilename(),".xml")){
			loadSqls(context,resource,context.isDefaultOverride());
			return true;
		}
	    return false;
    }
	
	protected void loadSqls(SqlReaderContext context, Resource resource, boolean defaultOverride) throws IOException {
        try(XmlReader reader = XML.createReader(resource) ){
        	reader.setPlaceholderResolver(context.getConfigContext().getAppContext().getConfig().getPlaceholderResolver());	
        	loadSqls(context, resource, reader, defaultOverride);
        }
	}

	protected void loadSqls(SqlReaderContext context, Resource resource, XmlReader reader, boolean defaultOverride) throws IOException{
		boolean foundValidRootElement = false;
		
		String dbType = resolveDbType(resource);
		if(!context.acceptDbType(dbType)){
			return;
		}
		
		while(reader.next()){
			if(reader.isStartElement(SQLS_ELEMENT)){
				foundValidRootElement = true;
				
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
							loadSqls(context,importResource,override);
							reader.nextToEndElement(IMPORT_ELEMENT);
						}
						continue;
					}
					
					if(reader.isStartElement(COMMAND_ELEMENT)){
						readSqlCommand(context, resource, reader, dbType, defaultOverride);
						continue;
					}
					if(reader.isStartElement(FRAGMENT_ELEMENT)){
						readSqlFragment(context, resource, reader, dbType, defaultOverride);
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

	private void readSqlFragment(SqlReaderContext context, Resource resource, XmlReader reader, String dbType, boolean defaultOverride) {
		String key 	   = reader.resolveAttribute(KEY_ATTRIBUTE);
		String content = reader.resolveElementTextAndEnd();

		if(Strings.isEmpty(key)){
			throw new SqlConfigException("'key' attribute must be defined in sql fragment, xml : " + reader.getSource());
		}

		String fragmentDescription = "[ key =" + key + "]";
		if(Strings.isEmpty(content)){
			throw new SqlConfigException("The content body of sql fragment " + fragmentDescription + " must not be empty, xml : " + reader.getSource());
		}
		log.debug("Loading sql fragment {} from [{}]",fragmentDescription,reader.getSource());

		OrmMetadata metadata = context.getConfigContext().getMetadata();

		metadata.addSqlFragment(key,new SimpleSqlFragment(reader.getSource(),content));
	}

	protected void readSqlCommand(SqlReaderContext context, Resource resource, XmlReader reader, String dbType, boolean defaultOverride){
		OrmMetadata metadata = context.getConfigContext().getMetadata();
		
		String  key                 = reader.resolveAttribute(KEY_ATTRIBUTE);
		String  name                = reader.resolveAttribute(NAME_ATTRIBUTE);
		String  langName     	    = reader.resolveAttribute(LANG_ATTRIBUTE);
		boolean override            = reader.resolveBooleanAttribute(OVERRIDE_ATTRIBUTE, defaultOverride);
		String	content             = reader.resolveElementTextAndEnd();
		String  entityName          = reader.resolveAttribute(ENTITY_NAME_ATTRIBUTE);
		String  entityClassName     = reader.resolveAttribute(ENTITY_CLASS_ATTRIBUTE);
		
		//check key,name,entity-class,entity-name
		if(Strings.isEmpty(key) && Strings.isEmpty(name)){
			throw new SqlConfigException("'key' or 'name' attribute must be defined in sql command, xml : " + reader.getSource());
		}
		
		if(!Strings.isEmpty(key) && Strings.containsWhitespaces(key)) {
			throw new SqlConfigException("'key' attribute cannot contains whitespace characters [" + key + "], xml : " + reader.getSource());
		}
		
		if(!Strings.isEmpty(name) && Strings.containsWhitespaces(name)) {
			throw new SqlConfigException("'name' attribute cannot contains whitespace characters [" + name + "], xml : " + reader.getSource());
		}
		
		if(Strings.isEmpty(key) && Strings.isEmpty(entityClassName) && Strings.isEmpty(entityName)){
			throw new SqlConfigException("'entity-class', or 'entity-name' attribute must be defined if 'name' attribute was defined, xml : " + reader.getSource());
		}
		
		String commandDescription = "[" + (Strings.isEmpty(key) ? "name=" + name + ",entity-class=" + entityClassName + ",entity-name=" + entityName : "key=" + key ) + "]";
		
		log.debug("Loading sql command {} from [{}]",commandDescription,reader.getSource());
		
		if(Strings.isEmpty(content = Strings.trim(content))){
			throw new SqlConfigException("The content body of sql command" + commandDescription + " must not be empty, xml : " + reader.getSource());
		}
		
		//find entity mapping of entity-class, entity-name
		EntityMapping em = null;
		
		if(!Strings.isEmpty(entityClassName)){
			Class<?> clazz = Classes.tryForName(entityClassName);
			if(null == clazz){
				throw new SqlConfigException("Class '" + entityClassName + "' not found, command" + commandDescription + 
											 ", xml : " + reader.getSource());
			}
			em = metadata.tryGetEntityMapping(clazz);
			if(null == em){
				log.info("Entity class '{}' not found in metadata of data source '{}'.", entityClassName, context.getConfigContext().getDb().getName());
				return;
				/*
				throw new SqlConfigException("Class '" + entityClassName + "' is not an entity class in command" + commandDescription + 
										     ", xml : " + reader.getSource());
										     */
			}
		}else if(!Strings.isEmpty(entityName)){
			em = metadata.tryGetEntityMapping(entityName);
			if(null == em){
				log.info("Entity name '{}' not found in metadata of data source '{}'.", entityName, context.getConfigContext().getDb().getName());
				return;
				//throw new SqlConfigException("Entity '" + entityName + "' not found, command" + commandDescription + ", xml : " + reader.getSource());
			}
		}
		
		//check is sql command exists
		if(!Strings.isEmpty(key)){
			SqlCommand existsCommand = metadata.tryGetSqlCommand(key);
			if(null != existsCommand){
				
				if(Strings.isEmpty(dbType) && !Strings.isEmpty(existsCommand.getDbType())){
					return;
				}
				
				//override
				if(!Strings.isEmpty(dbType) && Strings.isEmpty(existsCommand.getDbType())){
					override = true;
				}
				
				if(!override){
					throw new SqlConfigException("Found duplicated sql command key '" + key + "' in command" + commandDescription + 
							 ", xmls : " + existsCommand.getSource() + "," + reader.getSource());
				}
				
				metadata.removeSqlCommand(key);
			}
		}
		
		if(null != em && !Strings.isEmpty(name)){
			SqlCommand existsCommand = metadata.tryGetSqlCommand(em.getEntityName(),name);
			if(null != existsCommand){

				if(Strings.isEmpty(dbType) && !Strings.isEmpty(existsCommand.getDbType())){
					return;
				}
				
				//override
				if(!Strings.isEmpty(dbType) && Strings.isEmpty(existsCommand.getDbType())){
					override = true;
				}
				
				if(!override){
					throw new SqlConfigException("Found duplicated sql command name '" + name + "' of entity '" + em.getEntityName() + 
							 "' in command" + commandDescription + 
							 ", xmls : " + existsCommand.getSource() + "," + reader.getSource());
				}
				
				metadata.removeSqlCommand(em.getEntityName(),name);
				
				if(null != em.getEntityClass()){
					metadata.removeSqlCommand(em.getEntityClass(),name);	
				}
			}
		}
		
		SqlLanguage language = null;
		if(!Strings.isEmpty(langName)){
			language = beanFactory.tryGetBean(SqlLanguage.class,langName);
			if(null == language){
				throw new SqlConfigException("Sql language '" + langName + "' not found in command" + commandDescription +", xml : " + reader.getSource());
			}
		}else{
			language = defaultLanguage;
		}
		
		log.debug("SQL(s) : \n\n  {}\n",content);
		SqlCommand command = new DefaultSqlCommand(reader.getSource(), name, dbType, language, content);
		if(!Strings.isEmpty(key)){
			metadata.addSqlCommand(key, command);
		}
		
		if(null != em && !Strings.isEmpty(name)){
			metadata.addSqlCommand(em, name, command);
		}
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
