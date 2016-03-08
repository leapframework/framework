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
package leap.core.validation;

import leap.core.AppContext;
import leap.core.junit.AppTestBase;

import org.junit.Test;

import tested.beans.ValidBean;

public class ValidationTest extends AppTestBase {
	
	protected static ValidationManager manager = AppContext.factory().getBean(ValidationManager.class);

	@Test
	public void testBeanValidation() {
		Validation validation = manager.createValidation();
		
		ValidBean bean = new ValidBean();
		bean.s1 = null;
		bean.setS2("");
		
		validation.validate(bean);
		assertTrue(validation.hasErrors());
		
		Errors errors = validation.errors();
		assertEquals(2, errors.size());
	}
	
	@Test
	public void testLengthValidation() {
		Validation validation = manager.createValidation();
		
		ValidBean bean = new ValidBean();
		bean.setL1(null);
		assertFalse(validation.validate(bean).hasErrors());
		
		bean.setL1("s");
		assertTrue(validation.validate(bean).hasErrors());
	}
	
}
