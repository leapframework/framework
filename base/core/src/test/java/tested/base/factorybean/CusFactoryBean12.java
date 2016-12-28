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

package tested.base.factorybean;

import leap.core.BeanFactory;
import leap.core.annotation.Bean;
import leap.core.ioc.FactoryBean;

/**
 * Created by kael on 2016/12/28.
 */
@Bean(registerBeanFactory = true,targetType = {CusBean1.class,CusBean2.class})
public class CusFactoryBean12 implements FactoryBean {
    @Override
    public Object getBean(BeanFactory beanFactory, Class type) {
        if(type == CusBean1.class){
            return new CusBean1(this.getClass().getName());
        }else if(type == CusBean2.class){
            return new CusBean2(this.getClass().getName());
        }
        return null;
    }

    @Override
    public Object getBean(BeanFactory beanFactory, Class type, String name) {
        return getBean(beanFactory,type);
    }
}
