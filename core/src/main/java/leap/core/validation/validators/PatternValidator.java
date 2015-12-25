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

import leap.core.meta.MD;
import leap.core.validation.AbstractConstraintValidator;
import leap.core.validation.annotations.Pattern;
import leap.core.validation.annotations.Pattern.Flag;
import leap.lang.Args;
import leap.lang.Strings;

public class PatternValidator extends AbstractConstraintValidator<Pattern, CharSequence> {
	
	protected String				  name;
	protected java.util.regex.Pattern pattern;
	
	private String errorCode;
	
	public PatternValidator(String name, java.util.regex.Pattern pattern){
		Args.notEmpty(name,"name");
		Args.notNull(pattern,"pattern");
		this.name    = name;
		this.pattern = pattern;
		
		this.errorCode = "invalid" + Strings.upperFirst(name);
	}
	
	public PatternValidator(Pattern constraint, Class<?> valueType) {
	    super(constraint, valueType);
		this.name = constraint.name();
		
		if(!Strings.isEmpty(constraint.regexp())){
			this.pattern = compile(constraint);	
		}else{
			this.pattern = MD.getMPattern(name).getPattern();
		}
		
		this.errorCode = "invalid" + Strings.upperFirst(name);
    }

	@Override
    public String getErrorCode() {
	    return errorCode;
    }

	@Override
    protected boolean doValidate(CharSequence value) {
		if(null == value || value.length() == 0){
			return true;
 		}
		return pattern.matcher(value).matches();
    }
	
	protected java.util.regex.Pattern compile(Pattern constraint){
		Args.notNull(constraint,"pattern constraint");
		Args.notEmpty(constraint.regexp(),"pattern expression");
		
		Flag[] flags = constraint.flags();
		if(flags.length > 0){
			return java.util.regex.Pattern.compile(constraint.regexp(),intFlag(flags));
		}else{
			return java.util.regex.Pattern.compile(constraint.regexp());
		}
	}

	protected static final int intFlag(Flag[] flags){
		int intFlag = 0;
		for(int i=0;i<flags.length;i++){
			intFlag = intFlag | flags[i].getValue();
		}
		return intFlag;
	}
}