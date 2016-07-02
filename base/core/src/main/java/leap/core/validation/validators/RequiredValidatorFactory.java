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
package leap.core.validation.validators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import leap.core.validation.AbstractValidator;
import leap.core.validation.AbstractValidatorFactory;
import leap.core.validation.Validator;
import leap.core.validation.ValidatorFactory;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.Required;

public class RequiredValidatorFactory extends AbstractValidatorFactory implements ValidatorFactory {
	
	public static abstract class AbstractRequiredValidator<T> extends AbstractValidator<T> {
		@Override
        public String getErrorCode() {
	        return RequiredValidator.ERROR_CODE;
        }
	}
	
	protected static final Validator STRING = new AbstractRequiredValidator<CharSequence>(){
		@Override
        protected boolean doValidate(CharSequence value) {
			return null != value && value.length() > 0;
        }
	};
	
	protected static final Validator COLLECTION = new AbstractRequiredValidator<Collection<?>>() {
		@Override
        public boolean doValidate(Collection<?> value) {
	        return null != value && !value.isEmpty();
        }
	};
	
	protected static final Validator MAP = new AbstractRequiredValidator<Map<?,?>>() {
		@Override
        public boolean doValidate(Map<?, ?> value) {
	        return null != value && !value.isEmpty();
        }
	};
	
	protected static final Validator OBJECT_ARRAY = new AbstractRequiredValidator<Object[]>() {
		@Override
        public boolean doValidate(Object[] value) {
	        return null != value && value.length > 0;
        }
	};

	protected static final Validator ARRAY = new AbstractRequiredValidator<Object>() {
		@Override
        public boolean doValidate(Object value) {
	        return null != value && Array.getLength(value) > 0;
        }
	};
	
    public RequiredValidatorFactory() {
	    super();
    }

	@Override
    protected <T> AbstractValidator<T> doTryCreateValidator(Annotation constraint, Class<T> valueType) {
		if(constraint.annotationType().equals(NotEmpty.class) || constraint.annotationType().equals(Required.class)){
			return tryGetValidator(valueType);
		}
		return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> AbstractValidator<T> tryGetValidator(Class<T> valueType){
		if(CharSequence.class.isAssignableFrom(valueType)){
			return (AbstractValidator<T>)STRING;
		}

		if(Collection.class.isAssignableFrom(valueType)){
			return (AbstractValidator<T>)COLLECTION;
		}
		
		if(Object[].class.isAssignableFrom(valueType)){
			return (AbstractValidator<T>)OBJECT_ARRAY;
		}
		
		if(valueType.isArray()){
			return (AbstractValidator<T>)ARRAY;
		}
		
		if(Map.class.isAssignableFrom(valueType)){
			return (AbstractValidator<T>)MAP;
		}
		
		return (AbstractValidator<T>)RequiredValidator.INSTANCE;
    }
}