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
import java.util.Locale;

import leap.core.AppContext;
import leap.core.AppContextAware;
import leap.core.BeanFactory;
import leap.core.RequestContext;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.i18n.MessageSource;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.core.validation.annotations.Required;
import leap.core.validation.validators.NotNullValidator;
import leap.core.validation.validators.RequiredValidator;
import leap.core.validation.validators.RequiredValidatorFactory;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectConstructor;

public class DefaultValidationManager implements ValidationManager,AppContextAware {
	
	protected static final NotNullValidator 		NOT_NULL_VALIDATOR 	       = new NotNullValidator();
	protected static final RequiredValidator		REQUIRED_VALIDATOR         = new RequiredValidator();
	protected static final RequiredValidatorFactory REQUIRED_VALIDATOR_FACTORY = new RequiredValidatorFactory();
	
    protected @Inject @M MessageSource              messageSource;
    protected @Inject @M BeanFactory                beanFactory;
    protected @Inject @M BeanValidator              beanValidator;
    protected @Inject @M ValidatorFactory[]         validatorFactories;

    protected String defaultMessageKey = "validation.errors.invalidValue";
	
	@Override
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public String getDefaultMessageKey() {
		return defaultMessageKey;
	}

	public void setDefaultMessageKey(String defaultMessageKey) {
		this.defaultMessageKey = defaultMessageKey;
	}

	@Override
    public void setAppContext(AppContext appContext) {
		if(null == messageSource){
			messageSource = appContext.getMessageSource();
		}
    }

	@Override	
	public String getErrorMessage(Validator validator){
		return getErrorMessage(validator,(Locale)null);
	}
	
	@Override
    public String getErrorMessage(Validator validator, Locale locale) {
		String message = validator.getErrorMessage(messageSource, locale);

		if(null == message){
	    	message = messageSource.getMessage(locale, getDefaultMessageKey());
	    }
	    
	    return message;
    }
	
	@Override
	public String getErrorMessage(Validator validator, String title) {
		return getErrorMessage(validator, title, null);
	}
	
	@Override
	public String getErrorMessage(Validator validator, String title, Locale locale) {
		String message = validator.getErrorMessage(title,messageSource,locale);
		
	    if(null == message){
	    	//TODO : simple prepend title before error message without title. 
	    	message = title + ": " + getErrorMessage(validator, locale);
	    }
	    
	    return message;
	}

	public BeanValidator getBeanValidator() {
		return beanValidator;
	}

	public void setBeanValidator(BeanValidator beanValidator) {
		this.beanValidator = beanValidator;
	}

	@Override
    public Validator createValidator(Annotation constraint, Class<?> valueType) throws ObjectNotFoundException {
		Validator validator = tryCreateValidator(constraint, valueType);
		
		if(null == validator){
			throw new ObjectNotFoundException("Cannot create validator for the constraint '" + constraint.annotationType().getName() + 
											  "' and type '" + valueType.getName() + "'");
		}
		
	    return validator;
    }

	@Override
    public Validator tryCreateValidator(Annotation constraint, Class<?> valueType) {
		Args.notNull(constraint,"constraint annotation");
		Args.notNull(valueType,"value type");
		Class<? extends Annotation> constraintType = constraint.annotationType();
		String 					    constraintName = Strings.lowerFirst(constraintType.getSimpleName());
		
		ValidatorFactory factory = beanFactory.tryGetBean(ValidatorFactory.class, constraintName);
		if(null != factory){
			return factory.tryCreateValidator(constraint, valueType);
		}
		
		Validator validator = null;
		for(ValidatorFactory vf : this.validatorFactories){
			if((validator = vf.tryCreateValidator(constraint, valueType)) != null){
				return validator;
			}
		}
		
		ValidatedBy constraintAnnotation = constraint.annotationType().getAnnotation(ValidatedBy.class);
		if(null != constraintAnnotation){
			return createConstraintValidator(constraint, valueType, constraintAnnotation);
		}
		
		validator = tryGetOrCreateDefaultValidator(constraint,valueType);
		
		if(null == validator && constraintType.isAnnotationPresent(ConstraintAnnotation.class)){
			throw new IllegalStateException("Cannot create validator for constraint '" + constraintType.getName() + "'");
		}
		
		return validator;
    }
	
	@Override
    public Validation createValidation() {
	    return new DefaultValidation(this, new SimpleErrors(RequestContext.locale()));
    }

	@Override
    public Validation createValidation(Errors errors) {
	    return new DefaultValidation(this,errors);
    }

	protected Validator createConstraintValidator(Annotation constraint, Class<?> valueType, ValidatedBy constraintAnnotation){
		Class<? extends Validator> validatorClass = constraintAnnotation.validator();

		ReflectClass rc = ReflectClass.of(validatorClass);
		ReflectConstructor constructor = rc.getConstructor(constraint.annotationType(),Class.class);
		if(null == constructor){
			throw new IllegalStateException("Validator class '" + validatorClass.getName() + 
											"' must define constructor (" + constraint.annotationType().getName() + ", java.lang.Class)");
		}
		
		return constructor.newInstance(constraint,valueType);
	}

	protected Validator tryGetOrCreateDefaultValidator(Annotation constraint,Class<?> valueType){
		Class<?> constraintType = constraint.annotationType();
		
		if(constraintType.equals(NotNull.class)){
			return NOT_NULL_VALIDATOR;
		}
		
		if(constraintType.equals(Required.class) || constraintType.equals(NotEmpty.class)){
			return REQUIRED_VALIDATOR_FACTORY.tryCreateValidator(constraint, valueType);
		}
		
		return null;
	}
}