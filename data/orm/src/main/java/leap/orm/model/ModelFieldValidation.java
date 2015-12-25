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
package leap.orm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import leap.core.validation.Validator;
import leap.lang.Args;
import leap.orm.model.ModelRegistry.FieldInfo;
import leap.orm.model.ModelRegistry.ModelInfo;
import leap.orm.validation.FieldValidator;

public class ModelFieldValidation {
	
	private final List<FieldInfo> fields = new ArrayList<ModelRegistry.FieldInfo>();
	
	ModelFieldValidation(String className,String... fieldNames) {
		ModelInfo mi = ModelRegistry.getOrCreateModelInfo(className);
		for(String fieldName : fieldNames){
			fields.add(mi.getOrCreateField(fieldName));
		}
	}
	
	public ModelFieldValidation notEmpty(){
		return this;
	}
	
	public ModelFieldValidation length(int min){
		Args.assertTrue(min > 0 , "The 'min' value must higer than zero");
		return this;
	}
	
	public ModelFieldValidation length(int min,int max){
		return this;
	}
	
	public ModelFieldValidation pattern(Pattern pattern){
		return this;
	}
	
	public ModelFieldValidation pattern(Pattern pattern, String message){
		return this;
	}
	
	public ModelFieldValidation pattern(String regexp,int flags){
		return this;
	}
	
	public ModelFieldValidation pattern(String regexp,int flags,String message){
		return this;
	}
	
	public ModelFieldValidation with(Validator validator){
		//TODO :
		return this;
	}

	public ModelFieldValidation with(FieldValidator validator){
		for(FieldInfo fi : fields){
			fi.addValidator(validator);
		}
		return this;
	}
}