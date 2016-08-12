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

import leap.core.AppConfig;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;

public interface DomainConfigContext {
	
	AppConfig getAppConfig();
	
	String qualifyName(String entityDomainName,String fieldDomainName);
	
	EntityDomain tryGetEntityDomain(String name);
	
	FieldDomain tryGetFieldDomain(String qname);
	
	FieldDomain tryGetFieldDomain(Class<?> annotationType);
	
	void addEntityDomain(EntityDomain domain) throws ObjectExistsException;
	
	void addEntityDomainAlias(String alias,EntityDomain entity);
	
	void addEntityDomainAlias(String alias,String entityName) throws ObjectNotFoundException;
	
	void addFieldDomain(FieldDomain domain) throws ObjectExistsException;

    void addFieldDomain(FieldDomain domain, boolean override) throws ObjectExistsException;
	
	void addFieldDomain(Class<?> annotationType, FieldDomain domain) throws ObjectExistsException;
	
	void addFieldDomainAlias(String alias,String domainName) throws ObjectNotFoundException;
	
	void addFieldDomainAlias(String alias,FieldDomain domain);

}