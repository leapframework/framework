/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.core.jmx;

import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.BeanDefinition;
import leap.core.ioc.BeanDefinitionConfigurator;
import leap.core.ioc.BeanProcessor;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.jmx.MBeanExporter;
import leap.lang.jmx.Managed;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class JmxBeanProcessor implements BeanProcessor {

    private static final Log log = LogFactory.get(JmxBeanProcessor.class);

    private int counter = 1;

    protected @Inject MBeanExporter exporter;

    @Override
    public void postInitBean(AppContext context, BeanFactory factory, BeanDefinitionConfigurator c) throws Throwable {
        if(c.definition().isExportMBean() || Classes.isAnnotationPresent(c.definition().getBeanClass(), Managed.class)){
            c.setLazyInit(false);
        }
    }

    @Override
    public void postCreateBean(AppContext context, BeanFactory factory, BeanDefinition def, Object bean) throws Throwable {
        boolean exportMBean = def.isExportMBean();
        Managed a = bean.getClass().getAnnotation(Managed.class);

        if(null != a) {
            exportMBean = true;
        }

        if(exportMBean) {
            String name = def.getMBeanName();

            if(Strings.isEmpty(name)){
                name = null == a ?  null : a.name();
            }

            if(Strings.isEmpty(name)) {

                if(!Strings.isEmpty(def.getId())) {
                    name = def.getId();
                }else if(!Strings.isEmpty(def.getName())){
                    name = def.getType().getName() + "#" + def.getName();
                }else if(def.isPrimary()) {
                    name = def.getType().getName();
                }else {
                    name = def.getType().getName() + "-[mbean-" + String.valueOf(counter++) + "]";
                }

            }

            log.info("Export mbean '{}' of {}", name, def);

            exporter.export(name, bean);
        }
    }

}