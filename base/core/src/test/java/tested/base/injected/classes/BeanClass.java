/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package tested.base.injected.classes;

import leap.core.BeanFactory;
import leap.core.annotation.Bean;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;

import java.util.List;

/**
 * @author kael.
 */
@Bean(lazyInit = false)
public class BeanClass implements PostCreateBean{
    private @Inject Class<ParentClass1>[] classes1;
    private @Inject List<Class<ParentClass1>> classes2;
    private @Inject Class<? extends ParentClass2>[] classes3;
    private @Inject List<Class<? extends ParentClass2>> classes4;
    private @Inject List classes5;
    private @Inject Class<?>[] classes6;
    
    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        System.out.println(classes1.length);
        System.out.println(classes2.size());
    }

    public Class<ParentClass1>[] getClasses1() {
        return classes1;
    }

    public void setClasses1(Class<ParentClass1>[] classes1) {
        this.classes1 = classes1;
    }

    public List<Class<ParentClass1>> getClasses2() {
        return classes2;
    }

    public void setClasses2(List<Class<ParentClass1>> classes2) {
        this.classes2 = classes2;
    }

    public Class<? extends ParentClass2>[] getClasses3() {
        return classes3;
    }

    public void setClasses3(Class<? extends ParentClass2>[] classes3) {
        this.classes3 = classes3;
    }

    public List<Class<? extends ParentClass2>> getClasses4() {
        return classes4;
    }

    public void setClasses4(List<Class<? extends ParentClass2>> classes4) {
        this.classes4 = classes4;
    }

    public List getClasses5() {
        return classes5;
    }

    public void setClasses5(List classes5) {
        this.classes5 = classes5;
    }

    public Class<?>[] getClasses6() {
        return classes6;
    }

    public void setClasses6(Class<?>[] classes6) {
        this.classes6 = classes6;
    }
}
