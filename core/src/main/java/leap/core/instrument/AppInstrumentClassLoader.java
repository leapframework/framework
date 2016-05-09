/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.instrument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class AppInstrumentClassLoader extends ClassLoader {

    private ClassLoader parent;
    private Method      parentDefineClassMethod;

    public AppInstrumentClassLoader(ClassLoader parent){
        super(parent);

        this.parent = parent;

        try {
            parentDefineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass",
                    new Class[] {String.class, byte[].class, int.class,int.class});

            parentDefineClassMethod.setAccessible(true);
        } catch (Exception ignored) {
            //do nothing
        }
    }

    public Class<?> defineClass (String name, byte[] bytes) throws ClassFormatError {
        try {
            return (Class<?>)parentDefineClassMethod.invoke(parent, new Object[] {name, bytes, new Integer(0), new Integer(bytes.length)});
        } catch (InvocationTargetException e){
            throw new RuntimeException("Error instrument class '" + name + "', " + e.getTargetException().getMessage(), e.getTargetException());
        } catch (Exception e){
            throw new RuntimeException("Error instrument class '" + name + "', " + e.getMessage(), e);
        }
    }
}
