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
import leap.web.api.meta.desc.ApiDescContainer;
import leap.web.api.meta.desc.ModelDesc;

public class MApiModelBuilder extends MApiNamedWithDescBuilder<MApiModel> {

	protected MComplexType      	    type;
    protected ModelDesc                 desc;
    protected Class<?>                  javaType;
	protected List<MApiPropertyBuilder> properties = new ArrayList<MApiPropertyBuilder>();
	
	public MApiModelBuilder() {
	    super();
    }

	public MApiModelBuilder(MComplexType type, ApiDescContainer apiDescContainer) {
        this.type = type;
        this.name  = type.getName();
        this.title = type.getTitle();
        this.summary = type.getSummary();
        this.description = type.getDescription();

        this.javaType = type.getJavaType();

        this.desc = apiDescContainer.getModelDesc(this.javaType);

        this.properties.clear();
        for(MProperty mp : type.getProperties()) {
            addProperty(new MApiPropertyBuilder(mp,this.desc == null?null:this.desc.getPropertyDesc(mp.getName())));
        }
	}

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public List<MApiPropertyBuilder> getProperties() {
		return properties;
	}
	
	public void addProperty(MApiPropertyBuilder p) {
		properties.add(p);
	}

	@Override
    public MApiModel build() {
	    return new MApiModel(name, title, summary, description, javaType,
	    					Builders.buildArray(properties, new MApiProperty[properties.size()]), attrs);
    }
	
}
