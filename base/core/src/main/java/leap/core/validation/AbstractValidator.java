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

import leap.core.i18n.MessageSource;
import leap.lang.Arrays2;
import leap.lang.Strings;

public abstract class AbstractValidator<T> implements Validator {
	
	public static final String MESSAGE_KEY_PREFIX  = "validation.errors.";
	public static final String MESSAGE_KEY1_SUFFIX = ".1";
	public static final String MESSAGE_KEY2_SUFFIX = ".2";
	
	protected String   errorMessage;
	protected String   messageKey1;
	protected String   messageKey2;
	protected Object[] messageArguments1;
	
	public AbstractValidator() {
		
    }
	
	@Override
    public String getErrorMessage(MessageSource ms, Locale locale) {
		if(!Strings.isEmpty(errorMessage)){
			return errorMessage;
		}
		
		if(!Strings.isEmpty(getMessageKey1())){
			String m = ms.tryGetMessage(locale, messageKey1, getMessageArguments1());
			return null == m ? messageKey1 : m;
		}
		
	    return null;
    }

	@Override
    public String getErrorMessage(String title, MessageSource ms, Locale locale) {
		if(!Strings.isEmpty(errorMessage)){
			return errorMessage;
		}
		
		if(!Strings.isEmpty(getMessageKey2())){
			String m = ms.tryGetMessage(locale, messageKey2, getMessageArguments2(title));
			return null == m ? messageKey2 : m;
		}
		
	    return null;
    }

	public final String getMessageKey1() {
		if(null == messageKey1){
			messageKey1 = MESSAGE_KEY_PREFIX + getErrorCode() + MESSAGE_KEY1_SUFFIX;
		}
		return messageKey1;
	}
	
    public String getMessageKey2() {
		if(null == messageKey2){
			messageKey2 = MESSAGE_KEY_PREFIX + getErrorCode() + MESSAGE_KEY2_SUFFIX;
		}
	    return messageKey2;
    }

    public Object[] getMessageArguments1() {
		if(null == messageArguments1){
			messageArguments1 = createMessageArguments1();
		}
	    return messageArguments1;
    }
    
	protected Object[] getMessageArguments2(String title) {
		Object[] args = getMessageArguments1();
		if(null == args || args.length == 0){
			return new Object[]{title};
		}else{
			Object[] newArgs = new Object[args.length+1];
			newArgs[0] = title;
			for(int i=0;i<args.length;i++){
				newArgs[i+1] = args[i];
			}
			return newArgs;
		}
	}
	
	protected Object[] createMessageArguments1(){
		return Arrays2.EMPTY_OBJECT_ARRAY;
	}
	
    @Override
    @SuppressWarnings("unchecked")
    public final boolean validate(Object value) {
	    return doValidate((T)value);
    }

	protected abstract boolean doValidate(T value);
}