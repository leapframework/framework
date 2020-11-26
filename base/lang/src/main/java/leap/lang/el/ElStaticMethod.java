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
package leap.lang.el;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import leap.lang.Arrays2;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;

public class ElStaticMethod extends ElInstanceMethod implements ElFunction,ElMethod {

	public ElStaticMethod(Method m) {
	    super(m);
	    if(!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())){
	    	throw new IllegalArgumentException("Method '" + m + "' must be 'public static'");
	    }
    }

	public ElStaticMethod(ReflectMethod m) {
	    super(m);
    }

	@Override
    public Object invoke(ElEvalContext context, Object instance, Object[] args) throws Throwable {
	    return invokeStatic(context, args);
    }

	@Override
    public Object invoke(ElEvalContext context, Object[] args) throws Throwable {
		return invokeStatic(context, args);
	}

	protected Object invokeStatic(ElEvalContext context, Object[] args) {
		ReflectParameter[] parameters = m.getParameters();

		if (parameters.length > 0 && parameters[0].getType().equals(ElEvalContext.class)) {
			return m.invokeStatic(Arrays2.concat(new Object[]{context}, args));
		}
		return m.invokeStatic(args);
	}
}
