/*
 * Copyright 2014 the original author or authors.
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
package leap.lang.meta;

import static leap.lang.meta.MSimpleTypeFacet.FIXED_LENGTH;
import static leap.lang.meta.MSimpleTypeFacet.MAX_LENGTH;
import static leap.lang.meta.MSimpleTypeFacet.PRECISION;
import static leap.lang.meta.MSimpleTypeFacet.SCALE;


public enum MSimpleTypeKind {
	
	BYTE,
	BOOLEAN,
	STRING(MAX_LENGTH,FIXED_LENGTH),
	SMALLINT, //Short,INT16
	INTEGER, //Integer,INT32
	BIGINT,  //Long,INT64
	SINGLE,  //Float
	DOUBLE,
	DECIMAL(PRECISION,SCALE),
	DATETIME(PRECISION),
	TIME(PRECISION),
	BINARY(MAX_LENGTH,FIXED_LENGTH);
	
	private final MSimpleTypeFacet[] facets;
	
	private MSimpleTypeKind(MSimpleTypeFacet... facets){
		this.facets = facets;
	}

	public MSimpleTypeFacet[] getFacets() {
		return facets;
	}
	
	public boolean hasFacet(MSimpleTypeFacet facet){
		for(MSimpleTypeFacet f : this.facets){
			if(f.equals(facet)){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasMaxLengthFacet(){
		return hasFacet(MSimpleTypeFacet.MAX_LENGTH);
	}
	
	public boolean hasFixedLengthFacet(){
		return hasFacet(MSimpleTypeFacet.FIXED_LENGTH);
	}
	
	public boolean hasPrecisionFacet(){
		return hasFacet(MSimpleTypeFacet.PRECISION);
	}
	
	public boolean hasScaleFacet(){
		return hasFacet(MSimpleTypeFacet.SCALE);
	}
}
