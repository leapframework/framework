/*
 * Copyright 2015 the original author or authors.
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

import leap.lang.Builders;

import java.lang.reflect.Modifier;

public class MComplexTypeBuilder extends MStructuralTypeBuilder<MComplexType> {

	protected MComplexType baseType;
    protected Class<?>     javaType;
	protected boolean	   _abstract;

    public MComplexTypeBuilder() {

    }

    public MComplexTypeBuilder(Class<?> javaType) {
        this.javaType  = javaType;
        if(null != javaType) {
            this._abstract = Modifier.isAbstract(javaType.getModifiers());
        }
    }

    public boolean isAbstract() {
		return _abstract;
	}
	
	public void setAbstract(boolean a) {
		this._abstract = a;
	}
	
	public MComplexType getBaseType() {
		return baseType;
	}

	public void setBaseType(MComplexType baseType) {
		this.baseType = baseType;
	}

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public MComplexType build() {
	    return new MComplexType(name, title, summary, description,
                                baseType, javaType, Builders.buildList(properties), _abstract);
    }
	
}