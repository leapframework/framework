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
import leap.orm.generator.IdGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class XmlDomainSource implements DomainSource {

    private static final String DOMAINS          = "domains";
    private static final String IMPORT           = "import";
    private static final String RESOURCE         = "resource";
    private static final String CHECK_EXISTENCE  = "check-existence";
    private static final String DEFAULT_OVERRIDE = "default-override";
    private static final String OVERRIDE         = "override";
    private static final String DOMAIN           = "domain";
    private static final String ALIAS            = "alias";
    private static final String FIELD            = "field";
    private static final String FIELD_MAPPINGS   = "field-mappings";
    private static final String AUTO_MAPPING     = "auto-mapping";
    private static final String ENTITY_NAMES     = "entity-names";
    private static final String ENTITY_PATTERN   = "entity-pattern";
    private static final String NAME             = "name";
    private static final String TYPE             = "type";
    private static final String NULLABLE         = "nullable";
    private static final String LENGTH           = "length";
    private static final String PRECISION        = "precision";
    private static final String SCALE            = "scale";
    private static final String INSERT           = "insert";
    private static final String INSERT_VALUE     = "insert-value";
    private static final String UPDATE           = "update";
    private static final String UPDATE_VALUE     = "update-value";
    private static final String FILTERED         = "filtered";
    private static final String FILTERED_VALUE   = "filtered-value";
    private static final String DEFAULT_VALUE    = "default-value";
    private static final String SORT_ORDER       = "sort-order";
    private static final String COLUMN           = "column";
    private static final String ID_GENERATOR     = "id-generator";

    protected @Inject AppConfig   config;
    protected @Inject BeanFactory beanFactory;

	@Override
    public void loadDomains(Domains context) {
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
					reader.setPlaceholderResolver(config.getPlaceholderResolver());

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
			if(reader.isStartElement(DOMAINS)){
				foundValidRootElement = true;
				
				Boolean defaultOverrideAttribute = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE);
				if(null != defaultOverrideAttribute){
					context.setDefaultOverride(defaultOverrideAttribute);
				}
				
				while(reader.next()){
					if(reader.isStartElement(IMPORT)){
						boolean checkExistence    = reader.resolveBooleanAttribute(CHECK_EXISTENCE, true);
						boolean override          = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE, context.isDefaultOverride());
						String importResourceName = reader.resolveRequiredAttribute(RESOURCE);
						
						Resource importResource = Resources.getResource(resource,importResourceName);
						
						if(null == importResource || !importResource.exists()){
							if(checkExistence){
								throw new DomainConfigException("the import resource '" + importResourceName + "' not exists");	
							}
						}else{
                            LoadContext importContext = new LoadContext(context.domains);
							loadDomains(importContext, new SimpleAppResource(importResource, override));
							reader.nextToEndElement(IMPORT);
						}
						continue;
					}
					
					if(reader.isStartElement(DOMAIN)){
						readDomain(context, reader);
						continue;
					}

                    if(reader.isStartElement(FIELD_MAPPINGS)) {
                        readFieldMappings(context, reader);
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
	
    protected void readDomain(LoadContext context, XmlReader reader) {
        DomainBuilder builder = readField(context, reader);

        Domain domain = builder.build();

        //Add it.
        context.domains.addDomain(domain, builder.isOverride());

        //Aliases
        for(String alias : builder.getAliases()) {
            context.domains.addDomainAlias(domain.getName(), alias);
        }

    }

    protected void readFieldMappings(LoadContext context, XmlReader reader) {
        List<String> entityNames = parseWords(reader.getAttribute(ENTITY_NAMES));
        String p = reader.getAttribute(ENTITY_PATTERN);

        Pattern entityPattern = Strings.isEmpty(p) ? null : Pattern.compile(p, Pattern.CASE_INSENSITIVE);

        reader.loopInsideElement(() -> {

            if(reader.isStartElement(FIELD)) {
                DomainBuilder builder = readField(context, reader);

                String templateName = reader.getAttribute(DOMAIN);
                if(!Strings.isEmpty(templateName)) {
                    Domain templateDomain = context.domains.getDomain(templateName);
                    builder.tryUpdateFrom(templateDomain);
                }

                Domain domain = builder.build();

                if(!entityNames.isEmpty()) {
                    entityNames.forEach((entityName) ->  {
                        context.domains.addFieldMapping(entityName, domain);

                        builder.getAliases().forEach(alias -> {
                            context.domains.addFieldMappingAlias(entityName, domain.getName(), alias);
                        });
                    });
                }else if(null != entityPattern) {
                    context.domains.addFieldMapping(entityPattern, domain);

                    builder.getAliases().forEach(alias -> {
                        context.domains.addFieldMappingAlias(entityPattern, domain.getName(), alias);
                    });
                }else{
                    context.domains.addFieldMapping(domain);
                    builder.getAliases().forEach(alias -> {
                        context.domains.addFieldMappingAlias(domain.getName(), alias);
                    });
                }
            }

        });
    }
	
	protected DomainBuilder readField(LoadContext context, XmlReader reader){
		String  name         = reader.resolveAttribute(NAME);
        String  columnName   = reader.resolveAttribute(COLUMN);
		String  typeName     = reader.resolveAttribute(TYPE);
		Boolean nullable     = reader.resolveBooleanAttribute(NULLABLE);
		Integer length       = reader.resolveIntegerAttribute(LENGTH);
		Integer precision    = reader.resolveIntegerAttribute(PRECISION);
		Integer scale        = reader.resolveIntegerAttribute(SCALE);
		String  defaultValue = reader.resolveAttribute(DEFAULT_VALUE);
		Boolean insert       = reader.resolveBooleanAttribute(INSERT);
        String  insertValue  = reader.getAttribute(INSERT_VALUE);
		Boolean update       = reader.resolveBooleanAttribute(UPDATE);
		String  updateValue  = reader.getAttribute(UPDATE_VALUE);
        Boolean filter       = reader.resolveBooleanAttribute(FILTERED);
        String  filterValue  = reader.getAttribute(FILTERED_VALUE);
        String  idGenerator  = reader.getAttribute(ID_GENERATOR);
        boolean autoMapping  = reader.getBooleanAttribute(AUTO_MAPPING, false);
        Float sortOrder      = reader.getFloatAttribute(SORT_ORDER);
		boolean override     = reader.resolveBooleanAttribute(OVERRIDE, context.isDefaultOverride());

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
		
		Expression insertValueExpression = null;
		Expression updateValueExpression = null;
        Expression filterValueExpression = null;
		
		if(!Strings.isEmpty(insertValue)){
			insertValueExpression = EL.tryCreateValueExpression(insertValue);	
		}
		
		if(!Strings.isEmpty(updateValue)){
			updateValueExpression = EL.tryCreateValueExpression(updateValue);
		}

        if(!Strings.isEmpty(filterValue)){
            filterValueExpression = EL.tryCreateValueExpression(filterValue);
        }

        IdGenerator idGeneratorBean = null;
        if(!Strings.isEmpty(idGenerator)) {
            idGeneratorBean = beanFactory.tryGetBean(IdGenerator.class, idGenerator);
            if(null == idGeneratorBean) {
                throw new DomainConfigException("Id generator '" + idGenerator + "' not found, check the xml : " + reader.getCurrentLocation());
            }
        }

		return new DomainBuilder(reader.getSource())
										.setName(name)
                                        .setDefaultColumnName(columnName)
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
                                        .setFilter(filter)
                                        .setFilterValue(filterValueExpression)
                                        .setSortOrder(sortOrder)
                                        .addAliases(readAlias(reader))
                                        .setAutoMapping(autoMapping)
                                        .setIdGenerator(idGeneratorBean)
                                        .setOverride(override);
	}
	
    protected List<String> readAlias(XmlReader reader) {
        return parseWords(reader.getAttribute(ALIAS));
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
        private final Set<String> resources = new HashSet<>();
        private final Domains domains;
        private final boolean originalDefaultOverride;

        private boolean defaultOverride;

		private LoadContext(Domains domains){
			this.domains = domains;
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