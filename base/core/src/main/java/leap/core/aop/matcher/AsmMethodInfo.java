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

package leap.core.aop.matcher;

import leap.lang.asm.ASM;
import leap.lang.asm.Type;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;

import java.lang.annotation.Annotation;

public class AsmMethodInfo implements MethodInfo {

    private final Type       c;
    private final MethodNode m;

    public AsmMethodInfo(ClassNode c, MethodNode m) {
        this.c = Type.getObjectType(c.name);
        this.m = m;
    }

    public MethodNode getMethod() {
        return m;
    }

    @Override
    public String getClassName() {
        return c.getClassName();
    }

    @Override
    public String getName() {
        return m.name;
    }

    @Override
    public int getModifiers() {
        return m.access;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return ASM.isAnnotationPresent(m, annotationType);
    }

}
