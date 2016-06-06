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
package tested.basepackage.beans;

import leap.core.annotation.Bean;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Inject;

@Bean
public class TConstructorBean1 {

    private final TAnnotationBean2 bean;
    private final String           testConfigProperty;

    public TConstructorBean1(@Inject TAnnotationBean2 bean, @ConfigProperty String testConfigProperty) {
        this.bean = bean;
        this.testConfigProperty = testConfigProperty;
    }

    public TAnnotationBean2 getBean() {
        return bean;
    }

    public String getTestConfigProperty() {
        return testConfigProperty;
    }
}
