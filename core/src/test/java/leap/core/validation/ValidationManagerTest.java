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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import leap.core.AppContext;
import leap.core.junit.AppTestBase;
import leap.core.validation.annotations.Email;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.Pattern;
import leap.core.validation.validators.NotNullValidator;
import leap.core.validation.validators.RequiredValidator;
import leap.lang.Locales;

import org.junit.Test;

public class ValidationManagerTest extends AppTestBase {
	
	protected static ValidationManager manager = AppContext.factory().getBean(ValidationManager.class);
	
	protected static Map<String, Map<Class<? extends Annotation>, Annotation>> constraints = new HashMap<String, Map<Class<? extends Annotation>,Annotation>>();
	
	static {
		Field[] fields = ValidationAnnotations.class.getFields();
		for(Field field : fields){
			Map<Class<? extends Annotation>, Annotation> fieldConstraints = new HashMap<Class<? extends Annotation>, Annotation>();
			for(Annotation annotation : field.getAnnotations()){
				fieldConstraints.put(annotation.annotationType(), annotation);
			}
			constraints.put(field.getName(), fieldConstraints);
		}
	}
	
	@Test
	public void testNotEmptyValidator(){
		NotEmpty notEmpty = new NotEmpty() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return NotEmpty.class;
			}
		};
		
		testNotEmpty(manager.createValidator(notEmpty, String.class));
	}
	
	@Test
	public void testNotNullValidator(){
		Validator validator = new NotNullValidator();
		assertNotNull(validator);
		assertFalse(validator.validate(null));
		assertTrue(validator.validate(""));
		assertNotEmpty(manager.getErrorMessage(validator));
		testErrorMessage(validator);
	}
	
	@Test
	public void testPatternValidator(){
		Pattern pattern = constraint("regexValue",Pattern.class);
		Validator validator = manager.createValidator(pattern, String.class);
		assertNotNull(validator);
		assertNotSame(validator, manager.createValidator(pattern, String.class));
		testErrorMessage(validator);
	}
	
	@Test
	public void testEmailValidator(){
		Email email = constraint("email",Email.class);
		Validator validator = manager.createValidator(email, String.class);
		assertNotNull(validator);
		testErrorMessage(validator);
	}

	@Test
	public void testRequiredValidator(){
		Validator validator = new RequiredValidator();
		assertNotNull(validator);
		testErrorMessage(validator);
	}
	
	protected void testNotEmpty(Validator validator){
		assertTrue(validator.validate("x"));
		assertFalse(validator.validate(null));
		assertFalse(validator.validate(""));
		testErrorMessage(validator);
	}
	
	@SuppressWarnings("unchecked")
    protected static <T extends Annotation> T constraint(String field,Class<T> annotationType){
		Map<Class<? extends Annotation>, Annotation> annotations = constraints.get(field);
		T annotation = null;
		
		if(null != annotations){
			annotation = (T)annotations.get(annotationType);
		}
		
		if(annotation == null){
			throw new IllegalStateException("No constraint annotation type '" + annotationType.getName() + "' exists in field '" + field + "'");
		}
		
		return annotation;
	}
	
	protected void testErrorMessage(Validator validator){
		assertNotEmpty(manager.getErrorMessage(validator));
		assertNotEmpty(manager.getErrorMessage(validator,"x",Locales.DEFAULT_LOCALE));
	}
}
