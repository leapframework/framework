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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.orm.annotation.ADomain;

public class DefaultDomains implements Domains , DomainConfigContext, PostCreateBean {
	
    protected final Map<String, EntityDomain>  entityDomains     = new ConcurrentHashMap<String, EntityDomain>();
    protected final Map<String, EntityDomain>  entityAliases     = new ConcurrentHashMap<String, EntityDomain>();
    protected final Map<String, FieldDomain>   fieldDomains      = new ConcurrentHashMap<String, FieldDomain>();
    protected final Map<String, FieldDomain>   fieldAliases      = new ConcurrentHashMap<String, FieldDomain>();
    protected final Map<Class<?>, FieldDomain> typedFieldDomains = new ConcurrentHashMap<Class<?>, FieldDomain>();
	
    protected @Inject @M AppConfig             appConfig;
    protected @Inject @M DomainCreator         creator;
    protected @Inject @M DomainSource[]        domainSources;
	
	@Override
    public AppConfig getAppConfig() {
		return appConfig;
    }
	
	@Override
    public String qualifyName(String entityDomainName, String fieldDomainName) {
	    return qname(entityDomainName, fieldDomainName);
    }

	@Override
    public EntityDomain getEntityDomain(String name) throws ObjectNotFoundException {
		EntityDomain kind = tryGetEntityDomain(name);
		if(null == kind){
			throw new ObjectNotFoundException("Domain kind '" + name + "' not found");
		}
	    return kind;
    }

	@Override
    public FieldDomain getFieldDomain(String qualifiedName) throws ObjectNotFoundException {
		FieldDomain domain = tryGetFieldDomain(qualifiedName);
		
		if(null == domain){
			throw new ObjectNotFoundException("The field domain '" + qualifiedName + "' not found");
		}
		
	    return domain;
    }
	
	@Override
    public EntityDomain tryGetEntityDomain(String name) {
		Args.notNull(name,"name");
	    return entityDomains.get(Strings.lowerCase(name));
    }

	@Override
    public EntityDomain tryGetEntityDomainByAlias(String alias) {
		Args.notNull(alias,"alias");
	    return entityAliases.get(Strings.lowerCase(alias));
    }
	
	@Override
    public EntityDomain tryGetEntityDomainByNameOrAlias(String nameOrAlias) {
		EntityDomain entity = tryGetEntityDomain(nameOrAlias);
		
		if(null == entity){
			entity = tryGetEntityDomainByAlias(nameOrAlias);
		}
		
	    return entity;
    }

	@Override
    public FieldDomain tryGetFieldDomain(String qualifiedName) {
		Args.notEmpty(qualifiedName,"qualified name");
	    return fieldDomains.get(Strings.lowerCase(qualifiedName));
    }
	
	@Override
    public FieldDomain tryGetFieldDomainByAlias(String qualifiedAlias) {
	    return fieldAliases.get(Strings.lowerCase(qualifiedAlias));
    }
	
	@Override
    public FieldDomain tryGetFieldDomainByNameOrAlias(String qualifiedName) {
		FieldDomain domain = tryGetFieldDomain(qualifiedName);
		
		if(null == domain){
			domain = tryGetFieldDomainByAlias(qualifiedName);
		}
		
		return domain;
    }
	
	@Override
    public FieldDomain tryGetFieldDomain(EntityDomain entityDomain, String fieldName) {
		Args.notNull(entityDomain,"entity domain");
		
		FieldDomain domain = tryGetFieldDomainByNameOrAlias(qualifyName(entityDomain.getName(), fieldName));
		
		if(null == domain){
			domain = tryGetFieldDomainByNameOrAlias(fieldName);
		}
		
	    return domain;
    }

	@Override
    public FieldDomain tryGetFieldDomain(String entityName, String fieldName) {
		EntityDomain entity = tryGetEntityDomainByNameOrAlias(entityName);
		
		FieldDomain domain = null;
		if(null != entity){
			domain = tryGetFieldDomainByNameOrAlias(qname(entity.getName(),fieldName));
		}
		
		if(null == domain){
			domain = tryGetFieldDomainByNameOrAlias(fieldName);
		}
		
		return domain;
    }

	@Override
    public void addEntityDomain(EntityDomain domain) throws ObjectExistsException {
		String key = Strings.lowerCase(domain.getName());
		EntityDomain exists = entityDomains.get(key);
		
		if(null != exists){
			throw new ObjectExistsException("Entity domain '" + domain.getName() + " aleady exists in '" + exists.getSource() + "'");
		}
		
		entityDomains.put(key, domain);
    }

	@Override
    public void addEntityDomainAlias(String alias, EntityDomain domain) {
		String key = Strings.lowerCase(alias);
		entityAliases.put(key, domain);
    }

	@Override
    public void addEntityDomainAlias(String alias, String kindName) throws ObjectNotFoundException {
		addEntityDomainAlias(alias, getEntityDomain(kindName));
    }
	
	@Override
    public void addFieldDomain(FieldDomain domain) throws ObjectExistsException {
		String key = qkey(domain.getEntityName(),domain.getName());
		FieldDomain existsDomain = fieldDomains.get(key);

		//domin without kind can override exists domain with kind
		if(null != existsDomain){
			if(null != domain.getEntityDomain() || null == existsDomain.getEntityDomain()){
				throw new ObjectExistsException("The to be added domain '" + domain.getName() + "' aleady exists, check the source : " + domain.getSource() );	
			}
		}
		fieldDomains.put(key, domain);
		
		if(null == domain.getEntityDomain() && null == tryGetFieldDomain(domain.getName())){
			fieldDomains.put(Strings.lowerCase(domain.getName()), domain);
		}
    }
	
	@Override
    public FieldDomain tryGetFieldDomain(Class<?> annotationType) {
        return typedFieldDomains.get(annotationType);
    }

    @Override
    public void addFieldDomain(Class<?> annotationType, FieldDomain domain) throws ObjectExistsException {
        if(typedFieldDomains.containsKey(annotationType)) {
            throw new ObjectExistsException("Annotation type '" + annotationType + "' aleady exists");
        }
        typedFieldDomains.put(annotationType, domain);
    }

    @Override
    public void addFieldDomainAlias(String alias, String domainName) throws ObjectNotFoundException {
		addFieldDomainAlias(alias, getFieldDomain(domainName));
	}
	
	@Override
    public void addFieldDomainAlias(String alias, FieldDomain domain) {
		fieldAliases.put(qkey(domain.getEntityName(),alias), domain);
    }
	
	@Override
    public FieldDomain getOrCreateFieldDomain(Class<?> annotationType, ADomain domain) {
	    FieldDomain fd = tryGetFieldDomain(annotationType);
	    if(null != fd) {
	        return fd;
	    }
	    
	    fd = creator.createFieldDomainByAnnotation(this, annotationType, domain).build();
	    
	    addFieldDomain(annotationType, fd);
	    
	    return fd;
    }

    @Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		for(DomainSource source : domainSources){
			source.loadDomains(this);
		}
    }
	
	protected static String qname(String entity,String name){
		return Strings.isEmpty(entity) ? name : (entity + "." + name);
	}
	
	protected static String qkey(String entity,String name){
		return Strings.lowerCase(qname(entity,name));
	}
}