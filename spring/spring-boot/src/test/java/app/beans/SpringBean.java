/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.annotation.Order;

@Order(0)
public class SpringBean implements ListType{

    @Autowired
    protected LeapBean leapBean;

    @Autowired
    protected AutowireCapableBeanFactory autowireFactory;

    public LeapBean getLeapBean() {
        return leapBean;
    }

    public void setLeapBean(LeapBean leapBean) {
        this.leapBean = leapBean;
    }

    public AutowireCapableBeanFactory getAutowireFactory() {
        return autowireFactory;
    }

    public void setAutowireFactory(AutowireCapableBeanFactory autowireFactory) {
        this.autowireFactory = autowireFactory;
    }
}
