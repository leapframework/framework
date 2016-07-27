/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta.model;

import java.util.ArrayList;
import java.util.List;

import leap.lang.Builders;
import leap.lang.meta.MComplexType;
import leap.lang.meta.MProperty;

public class ApiModelBuilder extends ApiNamedWithDescBuilder<ApiModel> {
	
	protected MComplexType      	   type;
	protected List<ApiPropertyBuilder> properties = new ArrayList<ApiPropertyBuilder>();
	
	public ApiModelBuilder() {
	    super();
    }

	public ApiModelBuilder(MComplexType type) {
		this.setType(type);
	}

	public MComplexType getType() {
		return type;
	}

	public void setType(MComplexType type) {
		this.type = type;
		this.name  = type.getName();
		this.title = type.getTitle();
		this.summary = type.getSummary();
		this.description = type.getDescription();
		
		this.properties.clear();
		for(MProperty mp : type.getProperties()) {
			addProperty(new ApiPropertyBuilder(mp));
		}
	}

	public List<ApiPropertyBuilder> getProperties() {
		return properties;
	}
	
	public void addProperty(ApiPropertyBuilder p) {
		properties.add(p);
	}

	@Override
    public ApiModel build() {
	    return new ApiModel(name, title, summary, description, type, 
	    					Builders.buildArray(properties, new ApiProperty[properties.size()]), attrs);
    }
	
}
