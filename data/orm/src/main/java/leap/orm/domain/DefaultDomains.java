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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.orm.annotation.ADomain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class DefaultDomains implements Domains, PostCreateBean {

    protected final FieldDomainMappings              globalFieldMapping  = new FieldDomainMappings();
    protected final Map<String, FieldDomainMappings> entityFieldMappings = new ConcurrentHashMap<>();
    protected final Map<String, Domain>              domains             = new ConcurrentHashMap<String, Domain>();
    protected final Map<String, Domain>              domainAliases       = new ConcurrentHashMap<String, Domain>();
    protected final Map<Class<?>, Domain>            domainTypes         = new ConcurrentHashMap<Class<?>, Domain>();

    protected @Inject @M DomainCreator  creator;
    protected @Inject @M DomainSource[] domainSources;

    @Override
    public Domain autoMapping(String entityName, String fieldName) {
        for(FieldDomainMappings autoMapping : entityFieldMappings.values()) {
            if(autoMapping.hasEntityName()) {
                Domain domain = autoMapping.mapping(entityName, fieldName);
                if(null != domain) {
                    return domain;
                }
            }
        }

        for(FieldDomainMappings autoMapping : entityFieldMappings.values()) {
            if(autoMapping.hasEntityPattern()) {
                Domain domain = autoMapping.mapping(entityName, fieldName);
                if(null != domain) {
                    return domain;
                }
            }
        }

        Domain domain = globalFieldMapping.mapping(entityName, fieldName);
        if(null != domain) {
            return domain;
        }

        domain = tryGetDomain(fieldName);
        if(null != domain && domain.isAutoMapping()) {
            return domain;
        }

        return null;
    }

    @Override
    public void addFieldMapping(String entityName, Domain field) {
        getOrCreateFieldMappings(entityName).addField(field);
    }

    @Override
    public void addFieldMappingAlias(String entityName, String field, String alias) {
        getOrCreateFieldMappings(entityName).addAlias(field, alias);
    }

    @Override
    public void addFieldMapping(Pattern entityPattern, Domain field) {
        getOrCreateFieldMappings(entityPattern).addField(field);
    }

    @Override
    public void addFieldMappingAlias(Pattern entityPattern, String field, String alias) {
        getOrCreateFieldMappings(entityPattern).addAlias(field, alias);
    }

    @Override
    public void addFieldMapping(Domain field) {
        globalFieldMapping.addField(field);
    }

    @Override
    public void addFieldMappingAlias(String field, String alias) {
        globalFieldMapping.addAlias(field, alias);
    }

    protected FieldDomainMappings getOrCreateFieldMappings(String name) {
        String key = name.toLowerCase();

        FieldDomainMappings am = entityFieldMappings.get(key);
        if(null == am) {
            am = new FieldDomainMappings();
            am.setEntityName(name);
            entityFieldMappings.put(key, am);
        }
        return am;
    }

    protected FieldDomainMappings getOrCreateFieldMappings(Pattern pattern) {
        String key = pattern.pattern().toLowerCase();

        FieldDomainMappings am = entityFieldMappings.get(key);
        if(null == am) {
            am = new FieldDomainMappings();
            am.setEntityPattern(pattern);
            entityFieldMappings.put(key, am);
        }
        return am;
    }

    @Override
    public Domain getDomain(String name) throws ObjectNotFoundException {
		Domain domain = tryGetDomain(name);
		if(null == domain){
			throw new ObjectNotFoundException("The domain '" + name + "' not found");
		}
	    return domain;
    }

	@Override
    public Domain tryGetDomain(String name) {
        String key = name.toLowerCase();
	    Domain domain = domains.get(key);
        if(null == domain) {
            domain = domainAliases.get(key);
        }
        return domain;
    }

    @Override
    public Domain tryGetDomain(Class<?> annotationType) {
        return domainTypes.get(annotationType);
    }
	
    @Override
    public void addDomain(Domain domain, boolean override) throws ObjectExistsException {
        String key = domain.getName().toLowerCase();

        if(!override) {
            Domain existsDomain = domains.get(key);
            //domain without kind can override exists domain with kind
            if (null != existsDomain) {
                throw new ObjectExistsException("The to be added domain '" + domain.getName() + "' already exists, check the source : " + domain.getSource());
            }
        }

        domains.put(key, domain);
    }

    @Override
    public void addDomainAlias(String name, String alias) {
        Domain domain = tryGetDomain(name);
        if(null == domain) {
            throw new ObjectNotFoundException("The domain '" + name + "' not exists!");
        }

        String key = alias.toLowerCase();
        if(domainAliases.containsKey(key)) {
            throw new ObjectExistsException("The alias '" + alias + "' already exists!");
        }
        domainAliases.put(key, domain);
    }

    @Override
    public void addAnnotationType(Class<?> annotationType, Domain domain) throws ObjectExistsException {
        if(domainTypes.containsKey(annotationType)) {
            throw new ObjectExistsException("Annotation type '" + annotationType + "' already exists");
        }
        domainTypes.put(annotationType, domain);
    }

	@Override
    public Domain getOrCreateDomain(Class<?> annotationType, ADomain domain) {
	    Domain fd = tryGetDomain(annotationType);
	    if(null != fd) {
	        return fd;
	    }
	    
	    fd = creator.createFieldDomainByAnnotation(this, annotationType, domain).build();
	    
	    addAnnotationType(annotationType, fd);
	    
	    return fd;
    }

    @Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		for(DomainSource source : domainSources){
			source.loadDomains(this);
		}
    }
}