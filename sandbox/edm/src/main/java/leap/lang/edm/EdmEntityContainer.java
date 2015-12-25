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

public class EdmEntityContainer extends EdmNamedObject {

	private final boolean isDefault;
	
	private final boolean lazyLoadingEnabled;
	
	private final Enumerable<EdmEntitySet> entitySets;
	
	private final Enumerable<EdmFunctionImport> functionImports;
	
	private final Enumerable<EdmAssociationSet> associationSets;
	
	public EdmEntityContainer(String name,
							   boolean isDefault,boolean lazyLoadingEnabled,
							   Iterable<EdmEntitySet> entitySets,
							   Iterable<EdmFunctionImport> functionImports,
							   Iterable<EdmAssociationSet> associationSets) {
		
		super(name);
		this.isDefault = isDefault;
		this.lazyLoadingEnabled = lazyLoadingEnabled;
		this.entitySets 	  = Enumerables.of(entitySets);
		this.functionImports = Enumerables.of(functionImports);
		this.associationSets = Enumerables.of(associationSets);
	}
	
	public EdmEntityContainer(String name,
							   boolean isDefault,boolean lazyLoadingEnabled,
							   Iterable<EdmEntitySet> entitySets,
							   Iterable<EdmFunctionImport> functionImports,
							   Iterable<EdmAssociationSet> associationSets,
							   EdmDocumentation documentation) {

		this(name,isDefault,lazyLoadingEnabled,entitySets,functionImports,associationSets);

		this.documentation = documentation;
	}

	public boolean isDefault() {
    	return isDefault;
    }

	public boolean isLazyLoadingEnabled() {
    	return lazyLoadingEnabled;
    }

	public Enumerable<EdmEntitySet> getEntitySets() {
    	return entitySets;
    }

	public Enumerable<EdmFunctionImport> getFunctionImports() {
    	return functionImports;
    }

	public Enumerable<EdmAssociationSet> getAssociationSets() {
    	return associationSets;
    }
}