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
package leap.core.validation;

import java.lang.annotation.Annotation;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.reflect.ReflectAnnotation;
import leap.lang.reflect.ReflectAnnotation.AElement;

public abstract class AbstractConstraintValidator<A extends Annotation,T> extends AbstractValidator<T> {
	
	protected static final String KEY_PREFIX = "{";
	protected static final String KEY_SUFFIX = "}";

	public AbstractConstraintValidator() {
	    super();
    }
	
    public AbstractConstraintValidator(A constraint,Class<?> valueType){
		Args.notNull(constraint,"constraint");
		tryResolveErrorMessageFromAnnotation(constraint);
	}
    
    protected AbstractConstraintValidator(Annotation constraint) {
		Args.notNull(constraint,"constraint");
		tryResolveErrorMessageFromAnnotation(constraint);
    }
    
    protected void tryResolveErrorMessageFromAnnotation(Annotation a) {
		ReflectAnnotation ra = ReflectAnnotation.of(a.annotationType());
		AElement e = ra.tryGetElement("message");
		if(null != e){
			this.setErrorMessageOrKey((String)e.getValue(a));
		}
    }
    
    protected void setErrorMessageOrKey(String message) {
    	if(Strings.isEmpty(message)){
    		return;
    	}
    	
    	if(message.startsWith(KEY_PREFIX) && message.endsWith(KEY_SUFFIX)){
    		String key = message.substring(KEY_PREFIX.length(),message.length() - KEY_SUFFIX.length());
    		this.messageKey1 = key;
    		this.messageKey2 = key;
    	}else{
    		this.errorMessage = message;
    	}
    }
}