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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import leap.core.i18n.MessageSource;
import leap.core.meta.MSimpleValidation;
import leap.core.validation.validators.LengthValidator;
import leap.core.validation.validators.NotNullValidator;
import leap.core.validation.validators.RequiredValidator;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Error;
import leap.lang.NamedError;
import leap.lang.Strings;

public class DefaultValidation implements Validation {
	
	protected static final NotNullValidator  NOT_NULL_VALIDATOR = NotNullValidator.INSTANCE;
	protected static final RequiredValidator REQUIRED_VALIDATOR = RequiredValidator.INSTANCE;
	
	protected static final String INVALID_LENGTH_MESSAGE_KEY = "validation.errors.invalidLength";
	
	protected final ValidationManager manager;
	protected final MessageSource	  messageSource;
	protected final Errors 	   		  errors;
	
	public DefaultValidation(ValidationManager manager) {
		this(manager, null);
	}

	public DefaultValidation(ValidationManager manager, Errors errors) {
	    Args.notNull(manager, "manager");
		this.manager = manager;
		this.messageSource = manager.getMessageSource();
		this.errors = null == errors ? new SimpleErrors() : errors;
	}
	
	@Override
    public Locale locale() {
	    return errors.getLocale();
    }

	@Override
	public Errors errors() {
		return errors;
	}

	@Override
    public boolean hasErrors() {
	    return !errors.isEmpty();
    }
	
	@Override
    public boolean maxErrorsReached(int maxErrors) {
	    return errors.maxErrorsReached(maxErrors);
    }
	
	@Override
    public Validation addError(Error error) {
	    Args.notNull(error, "error object");
	    Args.notEmpty(error.getMessage(), "error message");
	    
	    String name = Strings.isEmpty(error.getCode()) ? UNNAMED : error.getCode();
	    String msg  = error.getMessage();
	    
	    errors.add(name, msg);
	    
        return this;
    }

    @Override
    public Validation addError(String name, String message) {
		Args.notEmpty(message,"message");
		errors.add(name, message);
	    return this;
    }
	
	@Override
    public Validation addError(String name, String messageKey, Object[] messageArguments) {
		return addError(name,messageKey,null,messageArguments);
    }

	@Override
    public Validation addError(String name, String messageKey, String defaultMessage) {
	    return addError(name, messageKey, defaultMessage, Arrays2.EMPTY_OBJECT_ARRAY);
    }

	@Override
    public Validation addError(String name, String messageKey, String defaultMessage, Object[] messageArguments) {
		Args.notEmpty(messageKey,"messageKey");
		
		String message = messageSource.tryGetMessage(messageKey, messageArguments);
		
		if(Strings.isEmpty(message)){
			message = defaultMessage;
		}
		
		return addError(name,message);
    }
	
	@Override
    public Validation addError(NamedError error) {
		if(null != error){
			this.errors.add(error);
		}
	    return this;
    }

	@Override
    public Validation addErrors(Errors errors) {
		if(null != errors){
			this.errors.addAll(errors);
		}
	    return this;
    }
	
	@Override
    public Validation addErrors(String prefix, Errors errors) {
		if(null != errors){
			this.errors.addAll(prefix, errors);
		}
	    return this;
    }

	@Override
    public Validation notNull(String name, Object value) {
	    return validate(name,value,NOT_NULL_VALIDATOR);
    }
	
	public boolean stateNotNull(String name,Object value) {
		return stateValidate(name,value,NOT_NULL_VALIDATOR);
	}

	@Override
	public Validation required(String name, Object value) {
		return validate(name,value,REQUIRED_VALIDATOR);
	}
	
	@Override
    public boolean stateRequired(String name, Object value) {
	    return stateValidate(name, value, REQUIRED_VALIDATOR);
    }

	@Override
    public Validation length(String name, CharSequence input, int min, int max) {
		if(!LengthValidator.validate(input, min, max)){
			addError(name, LengthValidator.MESSAGE_KEY, null, LengthValidator.getMessageArguments(input, min, max));
		}
	    return this;
    }

	@Override
    public boolean stateLength(String name, CharSequence input, int min, int max) {
		if(!LengthValidator.validate(input, min, max)){
			addError(name, LengthValidator.MESSAGE_KEY, null, LengthValidator.getMessageArguments(input, min, max));
			return false;
		}
	    return true;
    }
	
	@Override
    public Validation validate(Object bean) {
		if(null != bean){
			manager.getBeanValidator().validate(bean, this);	
		}
	    return this;
    }
	
	@Override
    public Validation validate(String name, Object bean) {
		stateValidate(name, bean);
	    return this;
    }

	@Override
    public boolean stateValidate(String name, Object bean) {
		Validation v = createNewValidation(name);
		
		if(!v.stateValidate(bean)){
			addErrors(v.errors());
			return false;
		}
		
		return true;
    }

	public boolean stateValidate(Object bean) {
		if(null != bean){
			return manager.getBeanValidator().validate(bean, this);	
		}
	    return true;
	}
	
	@Override
    public Validation validate(ValidatableBean bean) {
		if(null != bean && !bean.validate()){
			addErrors(bean.errors());
		}
	    return this;
    }
	
	public boolean stateValidate(ValidatableBean bean){
		if(null != bean && !bean.validate()){
			addErrors(bean.errors());
			return false;
		}
	    return true;
	}
	
	@Override
    public Validation validate(String name, ValidatableBean bean) {
		if(null != bean && !bean.validate()){
			errors.addAll(name, bean.errors());
		}
		return this;
    }
	
	public boolean stateValidate(String name, ValidatableBean bean){
		if(null != bean && !bean.validate()){
			errors.addAll(name, bean.errors());
			return false;
		}
		return true;
	}

	@Override
    public Validation validate(String name, Object value, Validator validator) {
		if(!validator.validate(value)){
			addError(validator, name, value);
		}
	    return this;
    }
	
	public boolean stateValidate(String name,Object value,Validator validator) {
		if(!validator.validate(value)){
			addError(validator, name, value);
			return false;
		}
	    return true;
	}
	
	@Override
    public Validation validate(String name, String title, Object value, Validator validator) {
		if(!validator.validate(value)){
			addError(validator, name, title, value);
		}
	    return this;
    }

	@Override
    public boolean stateValidate(String name, String title, Object value, Validator validator) {
		if(!validator.validate(value)){
			addError(validator, name, title, value);
			return false;
		}
	    return true;
    }

	@Override
    public Validation validate(MSimpleValidation m,Object value) {
		stateValidate(m,value);
		return this;
    }
	
	@Override
    public boolean stateValidate(MSimpleValidation m, Object value) {
		String name = m.getName();
		
		if(m.isRequired()){
			if(!stateValidate(name, m.getTitle(), value, REQUIRED_VALIDATOR)) {
				return false;
			}
		}
		
		boolean pass = true;
		
		if(null != value){
			Validator[] vs = m.getValidators();
			for(int i=0;i<vs.length;i++){
				Validator v = vs[i];
				if(!stateValidate(name, m.getTitle(), value, v) ){
					pass = false;
				}
			}
		}
		
	    return pass;
    }
	
    @Override
    public Validation validate(MSimpleValidation[] metadatas, Map<String, Object> values) {
    	validate(metadatas,values);
	    return this;
    }

	@Override
    @SuppressWarnings("unchecked")
    public boolean stateValidate(MSimpleValidation[] metadatas, Map<String, Object> values) {
		if(null == metadatas){
			return true;
		}
		if(null == values){
			values = Collections.EMPTY_MAP;
		}
		
		boolean pass = true;
		
		for(int i=0;i<metadatas.length;i++){
			MSimpleValidation m = metadatas[i];
			Object v = values.get(m.getName());
			if(!stateValidate(m, v)){
				pass = false;
			}
		}
		
		return pass;
    }

	@Override
    public Validation createNewValidation() {
	    return new DefaultValidation(manager);
    }

	@Override
    public Validation createNewValidation(String objectName) {
	    return new DefaultValidation(manager, new NestedSimpleErrors(objectName,errors.getLocale()));
    }

	protected void addError(Validator validator,String name,Object value){
		errors.add(name, manager.getErrorMessage(validator, errors.getLocale()));
	}
	
	protected void addError(Validator validator,String name, String title, Object value){
		errors.add(name, manager.getErrorMessage(validator, title, errors.getLocale()));
	}
}