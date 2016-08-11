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

package tests.core.ioc;

import leap.core.BeanFactory;
import leap.core.ioc.FactoryBean;
import org.junit.Test;
import tests.core.CoreTestCase;

/**
 * Created by kael on 2016/8/11.
 */
public class FactoryBeanTest extends CoreTestCase {
    @Test
    public void testBeanCreateFromFactoryBean(){
        TBeanHolder holder = factory.getBean(TBeanHolder.class);

        TBean1 bean2 = holder.getBean();
        TBean1 bean1 = factory.getBean("config");
        TBean2 bean3 = holder.getBean2();
        TBean2 bean4 = factory.getBean(TBean2.class);
        assertEquals(bean1,bean2);
        assertNotNull(bean3);
        assertNotNull(bean4);
        assertNotEquals(bean3,bean4);
    }

    public static class TFactoryBean implements FactoryBean<TBean1>{

        private TBean1 bean;

        @Override
        public TBean1 getBean(BeanFactory beanFactory, Class<TBean1> type) {
            if(bean == null){
                bean = new TBean1();
            }
            return bean;
        }

        @Override
        public TBean1 getBean(BeanFactory beanFactory, Class<TBean1> type, String name) {
            return getBean(beanFactory,type);
        }
    }
    public static class TFactoryBean2 implements FactoryBean<TBean2>{

        private TBean2 bean;

        @Override
        public TBean2 getBean(BeanFactory beanFactory, Class<TBean2> type) {
            if(bean == null){
                bean = new TBean2();
            }
            return bean;
        }

        @Override
        public TBean2 getBean(BeanFactory beanFactory, Class<TBean2> type, String name) {
            return getBean(beanFactory,type);
        }
    }
    public static class TBean1 {

    }
    public static class TBean2 {

    }
    public static class TBeanHolder{
        private TBean1 bean;
        private TBean2 bean2;
        public TBeanHolder(TBean1 bean,TBean2 bean2) {
            this.bean = bean;
            this.bean2 = bean2;
        }

        public TBean1 getBean() {
            return bean;
        }

        public void setBean(TBean1 bean) {
            this.bean = bean;
        }

        public TBean2 getBean2() {
            return bean2;
        }
    }
}
