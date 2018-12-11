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

package leap.spring.boot;

import leap.core.transaction.LocalTransactionProviderFactory;
import leap.core.transaction.TransactionProvider;
import leap.core.transaction.TransactionProviderFactory;
import leap.lang.annotation.Init;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public class LeapTransactionProviderFactory extends LocalTransactionProviderFactory implements TransactionProviderFactory {

    private static final Log log = LogFactory.get(LeapTransactionProviderFactory.class);

    private PlatformTransactionManager txm;

    @Init
    private void init() {
        if(null == Global.context()) {
            return;
        }
        try {
            txm = Global.context.getBean(PlatformTransactionManager.class);
            log.info("Spring PlatformTransactionManager found, use it");
        }catch (NoSuchBeanDefinitionException e){
            log.info("Spring PlatformTransactionManager not found, use local");
        }
    }

    @Override
    public TransactionProvider getTransactionProvider(DataSource dataSource, String name) {
        if(null == txm) {
            return super.getTransactionProvider(dataSource, name);
        }else {
            return new LeapTransactionProvider(txm,dataSource);
        }
    }

}