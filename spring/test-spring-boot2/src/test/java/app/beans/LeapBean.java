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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.beans;

import leap.core.annotation.Inject;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.List;

public class LeapBean implements ListType {

    @Inject
    protected SpringBean springBean;

    @Inject
    protected List<ListType> beans;

    @Inject
    protected AutowireCapableBeanFactory autowireFactory;

    public SpringBean getSpringBean() {
        return springBean;
    }

    public void setSpringBean(SpringBean springBean) {
        this.springBean = springBean;
    }

    public List<ListType> getBeans() {
        return beans;
    }

    public void setBeans(List<ListType> beans) {
        this.beans = beans;
    }

    public AutowireCapableBeanFactory getAutowireFactory() {
        return autowireFactory;
    }

    public void setAutowireFactory(AutowireCapableBeanFactory autowireFactory) {
        this.autowireFactory = autowireFactory;
    }
}
