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

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.orm.annotation.ADomain;

public interface Domains {
	
	String qualifyName(String entityDomainName,String fieldDomainName);
	
	EntityDomain getEntityDomain(String name) throws ObjectNotFoundException;
	
	FieldDomain getFieldDomain(String qualifiedName) throws ObjectNotFoundException;
	
	EntityDomain tryGetEntityDomain(String name);
	
	EntityDomain tryGetEntityDomainByAlias(String alias);
	
	EntityDomain tryGetEntityDomainByNameOrAlias(String nameOrAlias);
	
	/**
	 * Returns the {@link FieldDomain} of the given qualified name.
	 * 
	 * <p>
	 * Returns <code>null</code> if domain not exists of the given qualified name.
	 * 
	 * @see FieldDomain#getQualifiedName()
	 */
	FieldDomain tryGetFieldDomain(String qualifiedName);
	
	FieldDomain tryGetFieldDomain(Class<?> annotationType);
	
	FieldDomain tryGetFieldDomainByAlias(String qualifiedName);
	
	FieldDomain tryGetFieldDomainByNameOrAlias(String quanlifedName);
	
	FieldDomain tryGetFieldDomain(EntityDomain entityDomainName,String fieldDomainName);
	
	FieldDomain tryGetFieldDomain(String entityDomainName,String fieldDomainName);
	
	FieldDomain getOrCreateFieldDomain(Class<?> annotationType, ADomain domain);
	
	void addFieldDomain(Class<?> annotationType, FieldDomain domain) throws ObjectExistsException;
}