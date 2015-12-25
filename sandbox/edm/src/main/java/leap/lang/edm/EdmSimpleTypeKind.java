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

import static leap.lang.edm.EdmSimpleTypeFacet.*;

public enum EdmSimpleTypeKind {
	Binary(MaxLength,FixedLength),
	Boolean,
	Byte,
	DateTime(Precision),
	DateTimeOffset(Precision),
	Decimal(Precision,Scale),
	Double,
	Guid,
	Int16,
	Int32,
	Int64,
	SByte,
	Single,
	String(MaxLength,FixedLength),
	Stream(MaxLength,FixedLength),
	Time(Precision);
	
	private final EdmSimpleTypeFacet[] facets;
	
	private EdmSimpleTypeKind(EdmSimpleTypeFacet... facets){
		this.facets = facets;
	}

	public EdmSimpleTypeFacet[] getFacets() {
		return facets;
	}
	
	public boolean hasFacet(EdmSimpleTypeFacet facet){
		for(EdmSimpleTypeFacet f : this.facets){
			if(f.equals(facet)){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasMaxLengthFacet(){
		return hasFacet(EdmSimpleTypeFacet.MaxLength);
	}
	
	public boolean hasFixedLengthFacet(){
		return hasFacet(EdmSimpleTypeFacet.FixedLength);
	}
	
	public boolean hasPrecisionFacet(){
		return hasFacet(EdmSimpleTypeFacet.Precision);
	}
	
	public boolean hasScaleFacet(){
		return hasFacet(EdmSimpleTypeFacet.Scale);
	}
}