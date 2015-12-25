/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.edm;

import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.Iterables;
import leap.lang.Predicates;

public class EdmSchema extends EdmObjectWithDocumentation {
	
	private final String namespaceName;

	private final String alias;
	
	private final Enumerable<EdmEntityContainer> entityContainers;
	
	private final Enumerable<EdmEntityType> entityTypes;
	
	private final Enumerable<EdmAssociation> associations;
	
	private final Enumerable<EdmComplexType> complexTypes;
	
	private final Enumerable<EdmFunction> functions;
	
	private final Enumerable<EdmEnumType> enumTypes;
	
	public EdmSchema(String namespaceName,
			 		 String alias,
					 Iterable<EdmEntityContainer> entityContainers,
					 Iterable<EdmEntityType> entityTypes,
					 Iterable<EdmAssociation> associations,
					 Iterable<EdmComplexType> complexTypes,
					 Iterable<EdmEnumType> enumTypes,
					 Iterable<EdmFunction> functions) {
		
		this.namespaceName = namespaceName;
		this.alias         = alias;
		
		this.entityContainers = Enumerables.of(entityContainers);
		this.entityTypes      = Enumerables.of(entityTypes);
		this.associations     = Enumerables.of(associations);
		this.complexTypes     = Enumerables.of(complexTypes);
		this.enumTypes        = Enumerables.of(enumTypes);
		this.functions        = Enumerables.of(functions);
	}
	
	public EdmSchema(String namespaceName,
			 		 String alias,
					 Iterable<EdmEntityContainer> entityContainers,
					 Iterable<EdmEntityType> entityTypes,
					 Iterable<EdmAssociation> associations,
					 Iterable<EdmComplexType> complexTypes,
					 Iterable<EdmEnumType> enumTypes,
					 Iterable<EdmFunction> functions,
					 EdmDocumentation documentation) {
		
		this(namespaceName,alias,entityContainers,entityTypes,associations,complexTypes,enumTypes,functions);
		
		this.documentation = documentation;
	}

	public String getNamespaceName() {
    	return namespaceName;
    }

	public String getAlias() {
    	return alias;
    }
	
	public EdmEntityType findEntityType(String name){
		return Iterables.firstOrNull(entityTypes,Predicates.<EdmEntityType>nameEqualsIgnoreCase(name));
	}
	
	public EdmComplexType findComplexType(String name){
		return Iterables.firstOrNull(complexTypes,Predicates.<EdmComplexType>nameEqualsIgnoreCase(name));
	}

	public Enumerable<EdmEntityContainer> getEntityContainers() {
    	return entityContainers;
    }

	public Enumerable<EdmEntityType> getEntityTypes() {
    	return entityTypes;
    }

	public Enumerable<EdmAssociation> getAssociations() {
    	return associations;
    }

	public Enumerable<EdmComplexType> getComplexTypes() {
    	return complexTypes;
    }

	public Enumerable<EdmEnumType> getEnumTypes() {
		return enumTypes;
	}

	public Enumerable<EdmFunction> getFunctions() {
    	return functions;
    }
}