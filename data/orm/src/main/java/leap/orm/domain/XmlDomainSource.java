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
package leap.orm.domain;

import leap.core.*;
import leap.core.annotation.Inject;
import leap.core.el.EL;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.io.IO;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class XmlDomainSource implements DomainSource {
	
	private static final String DOMAINS_ELEMENT               = "domains";
	private static final String IMPORT_ELEMENT                = "import";
	private static final String ENTITY_DOMAIN_ELEMENT         = "entity-domain";
	private static final String ENTITY_DOMAIN_ALIAS_ELEMENT   = "entity-domain-alias";
	private static final String ALIAS_ELEMENT                 = "alias";
	private static final String FIELD_DOMAIN_ELEMENT          = "field-domain";
	private static final String FIELD_DOMAIN_ALIAS_ELEMENT    = "field-domain-alias";
	private static final String ALIAS_ATTRIBUTE               = "alias";
	private static final String DOMAIN_ATTRIBUTE              = "domain";
	private static final String ENTITY_DOMAIN_ATTRIBUTE       = "entity-domain";
	private static final String RESOURCE_ATTRIBUTE            = "resource";
	private static final String OVERRIDE_ATTRIBUTE            = "override";
	private static final String NAME_ATTRIBUTE                = "name";
	private static final String TYPE_ATTRIBUTE                = "type";
	private static final String NULLABLE_ATTRIBUTE            = "nullable";
	private static final String CHECK_EXISTENCE_ATTRIBUTE     = "check-existence";
	private static final String DEFAULT_OVERRIDE_ATTRIBUTE    = "default-override";
	private static final String LENGTH_ATTRIBUTE              = "length";
	private static final String PRECISION_ATTRIBUTE           = "precision";
	private static final String SCALE_ATTRIBUTE               = "scale";
	private static final String INSERT_ATTRIBUTE              = "insert";
	private static final String UPDATE_ATTRIBUTE              = "update";
	private static final String INSERT_VALUE_ATTRIBUTE        = "insert-value";
	private static final String UPDATE_VALUE_ATTRIBUTE        = "update-value";
	private static final String DEFAULT_VALUE_ATTRIBUTE       = "default-value";
	private static final String AUTO_MAPPING_ATTRIBUTE        = "auto-mapping";
    private static final String ENTITY_PATTERN                = "entity-pattern";
    private static final String SORT_ORDER                    = "sort-order";

    protected @Inject AppConfig config;
	
	@Override
    public void loadDomains(DomainConfigContext context) {
		loadDomains(new LoadContext(context), AppResources.get(config).search("domains"));
    }
	
	protected void loadDomains(LoadContext context, AppResource... resources){
		for(int i=0;i<resources.length;i++){
            AppResource ar = resources[i];
			Resource resource = ar.getResource();
			
			if(resource.isReadable() && resource.exists()){
				XmlReader reader = null;
				try{
					String resourceUrl = resource.getURL().toString();
					
					if(context.resources.contains(resourceUrl)){
						throw new AppConfigException("Cyclic importing detected, please check your config : " + resourceUrl);
					}

					context.resources.add(resourceUrl);
					
					reader = XML.createReader(resource);
					reader.setPlaceholderResolver(context.config.getPlaceholderResolver());

                    context.setDefaultOverride(ar.isDefaultOverride());
					loadDomains(context,resource,reader);
                    context.resetDefaultOverride();
				}catch(DomainConfigException e){
					throw e;
				}catch(Exception e){
					throw new DomainConfigException("Error loading domain from 'classpath:" + resource.getClasspath() + "', msg : " + e.getMessage(),e);
				}finally{
					IO.close(reader);
				}
			}
		}
	}
	
	protected void loadDomains(LoadContext context, Resource resource, XmlReader reader){
		boolean foundValidRootElement = false;
		
		while(reader.next()){
			if(reader.isStartElement(DOMAINS_ELEMENT)){
				foundValidRootElement = true;
				
				Boolean defaultOverrideAttribute = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE);
				if(null != defaultOverrideAttribute){
					context.setDefaultOverride(defaultOverrideAttribute);
				}
				
				while(reader.next()){
					if(reader.isStartElement(IMPORT_ELEMENT)){
						boolean checkExistence    = reader.resolveBooleanAttribute(CHECK_EXISTENCE_ATTRIBUTE, true);
						boolean override          = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE, context.isDefaultOverride());
						String importResourceName = reader.resolveRequiredAttribute(RESOURCE_ATTRIBUTE);
						
						Resource importResource = Resources.getResource(resource,importResourceName);
						
						if(null == importResource || !importResource.exists()){
							if(checkExistence){
								throw new DomainConfigException("the import resource '" + importResourceName + "' not exists");	
							}
						}else{
                            LoadContext importContext = new LoadContext(context.configContext);
							loadDomains(importContext, new SimpleAppResource(importResource, override));
							reader.nextToEndElement(IMPORT_ELEMENT);
						}
						continue;
					}
					
					if(reader.isStartElement(ENTITY_DOMAIN_ELEMENT)){
						readEntity(context, resource, reader);
						continue;
					}
					
					if(reader.isStartElement(ENTITY_DOMAIN_ALIAS_ELEMENT)){
						readEntityAlias(context, resource, reader);
						continue;
					}
					
					if(reader.isStartElement(FIELD_DOMAIN_ELEMENT)){
						readDomain(context, resource, reader, null);
						continue;
					}
					
					if(reader.isStartElement(FIELD_DOMAIN_ALIAS_ELEMENT)){
						readDomainAlias(context, resource, reader);
						continue;
					}
					
				}
				break;
			}
		}
		
		if(!foundValidRootElement){
			throw new DomainConfigException("valid root element not found in file : " + resource.getClasspath());
		}
	}
	
	protected void readEntity(LoadContext context,Resource resource,XmlReader reader){
		String name = reader.resolveRequiredAttribute(NAME_ATTRIBUTE);
		
		EntityDomain exists = context.configContext.tryGetEntityDomain(name);
		if(exists != null){
			throw new DomainConfigException("Entity domain '" + name + "' aleady exists in '" + exists.getSource() + "', check the xml : " + reader.getCurrentLocation());
		}
		
		EntityDomain entity = new EntityDomain(reader.getSource(), name); 
		
		context.configContext.addEntityDomain(entity);
		
		String alias = reader.resolveAttribute(ALIAS_ATTRIBUTE);
		if(!Strings.isEmpty(alias)){
			for(String word : parseWords(alias)){
				context.configContext.addEntityDomainAlias(word, entity);
			}
		}
		
		while(reader.next()){
			if(reader.isEndElement(ENTITY_DOMAIN_ELEMENT)){
				break;
			}
			
			if(reader.isStartElement(FIELD_DOMAIN_ELEMENT)){
				readDomain(context, resource, reader, entity);
				continue;
			}
			
			if(reader.isStartElement(ALIAS_ELEMENT)){
				for(String word : parseWords(reader.getElementTextAndEnd())){
					context.configContext.addEntityDomainAlias(word, entity);
				}
				continue;
			}
		}
	}
	
	protected void readEntityAlias(LoadContext context,Resource resource,XmlReader reader){
		String       entityName = reader.resolveRequiredAttribute(DOMAIN_ATTRIBUTE);
		EntityDomain entity     = context.configContext.tryGetEntityDomain(entityName);
		
		if(null == entity){
			throw new DomainConfigException("Entity domain '" + entityName + "' not found, check the location : " + reader.getCurrentLocation());
		}
		
		for(String word : parseWords(reader.resolveAttribute(ALIAS_ATTRIBUTE))){
			context.configContext.addEntityDomainAlias(word, entity);
		}
		
		for(String word : parseWords(reader.getElementTextAndEnd())){
			context.configContext.addEntityDomainAlias(word, entity);
		}
	}
	
	protected void readDomain(LoadContext context,Resource resource,XmlReader reader, EntityDomain entityDomain){
		String  entityName   = reader.resolveAttribute(ENTITY_DOMAIN_ATTRIBUTE);
		String  name         = reader.resolveAttribute(NAME_ATTRIBUTE);
		String  typeName     = reader.resolveAttribute(TYPE_ATTRIBUTE);
		Boolean nullable     = reader.resolveBooleanAttribute(NULLABLE_ATTRIBUTE);
		Integer length       = reader.resolveIntegerAttribute(LENGTH_ATTRIBUTE);
		Integer precision    = reader.resolveIntegerAttribute(PRECISION_ATTRIBUTE);
		Integer scale        = reader.resolveIntegerAttribute(SCALE_ATTRIBUTE);
		String  defaultValue = reader.resolveAttribute(DEFAULT_VALUE_ATTRIBUTE);
		Boolean insert       = reader.resolveBooleanAttribute(INSERT_ATTRIBUTE);
		Boolean update       = reader.resolveBooleanAttribute(UPDATE_ATTRIBUTE);
		String  insertValue  = reader.getAttribute(INSERT_VALUE_ATTRIBUTE);
		String  updateValue  = reader.getAttribute(UPDATE_VALUE_ATTRIBUTE);
		boolean	autoMapping  = reader.getBooleanAttribute(AUTO_MAPPING_ATTRIBUTE,false);
        String entityPattern = reader.getAttribute(ENTITY_PATTERN);
        Float sortOrder      = reader.getFloatAttribute(SORT_ORDER);
		boolean override     = reader.resolveBooleanAttribute(OVERRIDE_ATTRIBUTE, context.isDefaultOverride());
		
		if(!Strings.isEmpty(entityName)){
			entityDomain = context.configContext.tryGetEntityDomain(entityName);
			if(null == entityDomain){
				throw new DomainConfigException("Entity domain '" + entityName + "' not found, check the xml : " + reader.getCurrentLocation());
			}
		}
		
		//check name
		if(Strings.isEmpty(name)){
			throw new DomainConfigException("The 'name' and 'type' attribute must be defined in domain, check the xml : " + reader.getCurrentLocation());
		}
		
		JdbcType type = null;
		if(!Strings.isEmpty(typeName)) {
			type = JdbcTypes.tryForTypeName(typeName);
			if(null == type){
				throw new DomainConfigException("Jdbc type '" + typeName + "' not supported, check the xml : " + reader.getCurrentLocation());
			}
		}
		
		//check is domain exists
		if(!override){
			String qname = context.configContext.qualifyName(null == entityDomain ? null : entityDomain.getName(), name);
			FieldDomain fieldDomain = context.configContext.tryGetFieldDomain(qname);
			if(null != fieldDomain){
				throw new DomainConfigException(Strings.format(
						"Found duplicated field domain '" + name + "' in xmls : " + fieldDomain.getSource(), reader.getCurrentLocation()));
			}
		}
		
		Expression insertValueExpression = null;
		Expression updateValueExpression = null;
		
		if(!Strings.isEmpty(insertValue)){
			insertValueExpression = EL.tryCreateValueExpression(insertValue);	
		}
		
		if(!Strings.isEmpty(updateValue)){
			updateValueExpression = EL.tryCreateValueExpression(updateValue);
		}
		
		FieldDomain domain = new FieldDomainBuilder(reader.getSource())
										.setEntityDomain(entityDomain)
										.setName(name)
										.setType(type)
										.setNullable(nullable)
										.setLength(length)
										.setPrecision(precision)
										.setScale(scale)
										.setDefaultValue(defaultValue)
										.setInsert(insert)
										.setUpdate(update)
										.setInsertValue(insertValueExpression)
										.setUpdateValue(updateValueExpression)
										.setAutoMapping(autoMapping)
                                        .setEntityPattern(Strings.isEmpty(entityPattern) ? null : Pattern.compile(entityPattern))
                                        .setSortOrder(sortOrder)
										.build();
		
		context.configContext.addFieldDomain(domain, override);
		
		String alias = reader.resolveAttribute(ALIAS_ATTRIBUTE);
		if(!Strings.isEmpty(alias)){
			readWords(context, domain, Strings.trim(alias));
		}
		
		while(reader.next()){
			
			if(reader.isEndElement(FIELD_DOMAIN_ELEMENT)){
				break;
			}
			
			if(reader.isStartElement(ALIAS_ATTRIBUTE)){
				readWords(context, domain, Strings.trim(reader.getElementTextAndEnd()));
				continue;
			}
		}
	}
	
	protected void readDomainAlias(LoadContext context,Resource resource,XmlReader reader){
		String domainName = reader.resolveRequiredAttribute(DOMAIN_ATTRIBUTE);
		FieldDomain domain     = context.configContext.tryGetFieldDomain(domainName);
		
		if(null == domain){
			throw new DomainConfigException("Domain '" + domainName + "' not found, check the location : " + reader.getCurrentLocation());
		}
		
		readWords(context, domain, reader.resolveAttribute(ALIAS_ATTRIBUTE));
		readWords(context, domain, reader.resolveElementTextAndEnd());
	}
	
	protected void readWords(LoadContext context,FieldDomain domain,String words){
		for(String word : parseWords(words)){
			context.configContext.addFieldDomainAlias(word, domain);
		}
	}
	
	protected List<String> parseWords(String words){
		List<String> list = new ArrayList<String>();
		if(!Strings.isEmpty(words)){
			String[] lines = Strings.splitMultiLines(words);
			for(String line : lines){
				for(String word : Strings.split(line,',')){
					list.add(word);
				}
			}
		}
		return list;
	}
	
	private final class LoadContext {
		private final Set<String>         resources = new HashSet<>();
		private final DomainConfigContext configContext;
		private final AppConfig			  config;
        private final boolean             originalDefaultOverride;

        private boolean defaultOverride;
		
		private LoadContext(DomainConfigContext context){
			this.configContext = context;
			this.config		   = context.getAppConfig();
            this.originalDefaultOverride = false;
            this.defaultOverride = false;
		}

        public boolean isDefaultOverride() {
            return defaultOverride;
        }

        public void setDefaultOverride(boolean b) {
            this.defaultOverride = b;
        }

        public void resetDefaultOverride() {
            this.defaultOverride = originalDefaultOverride;
        }
	}
	
}