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

import java.util.regex.Pattern;

public interface Domains {

    /**
     * Returns the auto mapping domain or null if not exists.
     */
    Domain autoMapping(String entityName, String fieldName);

    /**
     *
     */
    void addFieldMapping(String entityName, Domain field);

    /**
     *
     */
    void addFieldMappingAlias(String entityName, String field, String alias);

    /**
     *
     */
    void addFieldMapping(Pattern entityPattern, Domain field);

    /**
     *
     */
    void addFieldMappingAlias(Pattern entityPattern, String field, String alias);

    /**
     *
     */
    void addFieldMapping(Domain field);

    /**
     *
     */
    void addFieldMappingAlias(String field, String alias);

    /**
     *
     */
    Domain getDomain(String name) throws ObjectNotFoundException;

    /**
     * Returns the {@link Domain} with the name or null if not exists.
     */
    Domain tryGetDomain(String name);

    /**
     * Returns the {@link Domain} associated with the annotation type or null if not exists.
     */
    Domain tryGetDomain(Class<?> annotationType);

    /**
     * Adds a new {@link Domain}
     */
    default void addDomain(Domain domain) throws ObjectExistsException {
        addDomain(domain, false);
    }

    /**
     * Adds a new {@link Domain}
     */
    void addDomain(Domain domain, boolean override) throws ObjectExistsException;

    /**
     * Adds a alias domain.
     */
    void addDomainAlias(String domain, String alias);

    /**
     * Adds a new annotation type associate with a domain.
     */
    void addAnnotationType(Class<?> annotationType, Domain domain) throws ObjectExistsException;

    /**
     *
     */
    Domain getOrCreateDomain(Class<?> annotationType, ADomain domain);

}