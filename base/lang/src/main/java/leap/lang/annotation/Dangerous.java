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
package leap.lang.annotation;

import java.lang.annotation.Documented;

import leap.lang.Confirm;
import leap.lang.Confirm.ConfirmException;

/**
 * Indicates the annotated action is dangerous, i.e. deletes all data.
 * 
 * <p>
 * Be careful to perform an action annotated as dangerous.
 * 
 * <p>
 * The annotated object may ask for confirm of the danger.
 * 
 * If so, the method {@link Confirm#run(leap.lang.Action)} must be used to run this action.
 */
@Documented
public @interface Dangerous {
	
	/**
	 * Invoking a method annotated with {@link Dangerous} and sets {@link Dangerous#askForConfirm()} to <code>true</code>
	 * 
	 * will throw {@link ConfirmException} if not confirm to perform the dangerous action.
	 * 
	 * <p>
	 * Can invoke {@link Confirm#run(leap.lang.Action)} to confirm the danger.
	 */
	boolean askForConfirm() default false;

}