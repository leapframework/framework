/*
 * Copyright 2014 the original author or authors.
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
package leap.core.meta;

import java.util.ArrayList;
import java.util.List;

import leap.core.validation.Validator;
import leap.core.validation.validators.LengthValidator;
import leap.core.validation.validators.MaxValidator;
import leap.core.validation.validators.MinValidator;
import leap.core.validation.validators.PatternValidator;
import leap.lang.Buildable;
import leap.lang.Strings;
import leap.lang.meta.MPattern;
import leap.lang.meta.MSimpleType;

public class MSimpleParameter extends MParameterBase implements MSimpleValidation {
	
	private static final Validator[] EMPTY_VALIDATORS = new Validator[]{};
	
	protected final boolean		secret;
	protected final Object      defaultValue;
	protected final Integer     minLength;
	protected final Integer     maxLength;
	protected final Long	    minValue;
	protected final Long	    maxValue;
	protected final MPattern    pattern;
	protected final Validator[] validators;

	public MSimpleParameter(String name, String title, String summary, String description, boolean secret,
							MSimpleType type, Boolean required, Object defaultValue,
							Integer minLength,Integer maxLength,Long minValue,Long maxValue,MPattern pattern,
							Validator[] validators) {
		
		super(name, title, summary, description, type, required);
		
		this.secret		  = secret;
		this.defaultValue = type.valueOf(defaultValue);
		this.minLength    = minLength;
		this.maxLength    = maxLength;
		this.minValue     = minValue;
		this.maxValue     = maxValue;
		this.pattern      = pattern;
		this.validators   = null == validators ? EMPTY_VALIDATORS : validators;
	}
	
	/**
	 * Returns <code>true</code> if this parameter should be protected, such as password.
	 */
	public boolean isSecret() {
		return secret;
	}

	public MSimpleType getType() {
		return (MSimpleType)type;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	@Override
    public boolean isRequired() {
	    return null == required ? false : required;
    }

    public Integer getMinLength() {
	    return minLength;
    }

    public Integer getMaxLength() {
	    return maxLength;
    }
	
    public Long getMinValue() {
	    return minValue;
    }

    public Long getMaxValue() {
	    return maxValue;
    }

    public MPattern getPattern() {
	    return pattern;
    }
    
	@Override
    public Validator[] getValidators() {
	    return validators;
    }

	public static class Builder extends MParameterBase.Builder implements Buildable<MSimpleParameter> {
		
		protected boolean	  secret;
		protected Object 	  defaultValue;
		protected Integer     minLength;
		protected Integer     maxLength;
		protected Long		  minValue;
		protected Long		  maxValue;
		protected String	  pattern;
		
		public Builder() {
	        super();
        }
		
		public Builder(MSimpleParameter p) {
			super(p);
			this.secret = p.isSecret();
			this.type = (MSimpleType)p.type;
			this.defaultValue = p.defaultValue;
			this.minLength = p.minLength;
			this.maxLength = p.maxLength;
			this.minValue = p.minValue;
			this.maxValue = p.maxValue;
			this.pattern = p.pattern == null ? null : p.pattern.getName();
		}
		
		public boolean isSecret() {
			return secret;
		}

		public void setSecret(boolean secret) {
			this.secret = secret;
		}

		public MSimpleType getDataType() {
			return (MSimpleType)type;
		}

		public void setDataType(MSimpleType dataType) {
			this.type = dataType;
		}
		
		public Object getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}
		
		public Integer getMinLength() {
			return minLength;
		}

		public void setMinLength(Integer minLength) {
			this.minLength = minLength;
		}

		public Integer getMaxLength() {
			return maxLength;
		}

		public void setMaxLength(Integer maxLength) {
			this.maxLength = maxLength;
		}

		public Long getMinValue() {
			return minValue;
		}

		public void setMinValue(Long minValue) {
			this.minValue = minValue;
		}

		public Long getMaxValue() {
			return maxValue;
		}

		public void setMaxValue(Long maxValue) {
			this.maxValue = maxValue;
		}

		public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		@Override
        public MSimpleParameter build() {
			MPattern p = !Strings.isEmpty(pattern) ? MD.getMPattern(pattern) : null;
			
			List<Validator> validators = new ArrayList<>();
			if(null != minLength || null != maxLength){
				validators.add(new LengthValidator(minLength == null ? 0 : minLength, maxLength == null ? Integer.MAX_VALUE : maxLength));
			}
			
			if(null != minValue){
				validators.add(new MinValidator(minValue));
			}
			
			if(null != maxValue){
				validators.add(new MaxValidator(maxValue));
			}
			
			if(null != p){
				validators.add(new PatternValidator(p.getName(),p.getPattern()));
			}
			
	        return new MSimpleParameter(name, title, summary, description, secret,
	        						    (MSimpleType)type, required, defaultValue,
	        						    minLength,maxLength,minValue,maxValue,p,validators.toArray(new Validator[validators.size()]));
        }
	}

}