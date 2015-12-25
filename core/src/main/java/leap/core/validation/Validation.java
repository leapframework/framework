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

import java.util.Locale;
import java.util.Map;

import leap.core.meta.MSimpleValidation;
import leap.lang.Error;
import leap.lang.NamedError;

public interface Validation extends ValidationContext {
    
    String UNNAMED = "unnamed";
	
	@Override
	default Validation validation() {
		return this;
	}
	
	/**
	 * @se {@link Errors#getLocale()}
	 */
	Locale locale();
	
	Errors errors();

	boolean hasErrors();
	
	/**
	 * @see Errors#maxErrorsReached(int)
	 */
	boolean maxErrorsReached(int maxErrors);
	
	Validation createNewValidation();
	
	Validation createNewValidation(String objectName);
	
	Validation addError(String name,String message);
	
	Validation addError(String name,String messageKey,Object[] messageArguments);
	
	Validation addError(String name,String messageKey,String defaultMessage);
	
	Validation addError(String name,String messageKey,String defaultMessage,Object[] messageArguments);
	
	Validation addError(Error error);
	
	Validation addError(NamedError error);
	
	Validation addErrors(Errors errors);
	
	Validation addErrors(String prefix,Errors errors);
	
	Validation notNull(String name,Object value);
	
	Validation required(String name,Object value);
	
	Validation length(String name,CharSequence input,int min,int max);
	
	Validation validate(Object bean);
	
	Validation validate(String name, Object bean);
	
	Validation validate(ValidatableBean bean);
	
	Validation validate(String name,ValidatableBean bean);
	
	Validation validate(String name,Object value,Validator validator);
	
	Validation validate(String name,String title,Object value, Validator validator);
	
	Validation validate(MSimpleValidation metadata, Object value);
	
	Validation validate(MSimpleValidation[] metadatas,Map<String, Object> values);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateNotNull(String name,Object value);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateRequired(String name,Object value);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateLength(String name,CharSequence intpu,int min,int max);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(Object bean);

	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(String name, Object bean);

	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(ValidatableBean bean);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(String name,ValidatableBean bean);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(String name,Object value, Validator validator);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(String name,String title,Object value,Validator validator);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(MSimpleValidation metadata,Object value);
	
	/**
	 * Returns <code>false</code> if the validation not passed.
	 */
	boolean stateValidate(MSimpleValidation[] metadatas, Map<String, Object> values);
}