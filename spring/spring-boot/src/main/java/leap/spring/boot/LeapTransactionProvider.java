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

import leap.core.transaction.*;
import leap.lang.Try;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;

public class LeapTransactionProvider extends AbstractTransactionProvider implements TransactionProvider {

    private static final DefaultTransactionDefinition REQUIRED =
            new DefaultTransactionDefinition(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED);

    private static final DefaultTransactionDefinition REQUIRES_NEW =
            new DefaultTransactionDefinition(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    private final PlatformTransactionManager txm;
    private final DataSource                 dataSource;

    public LeapTransactionProvider(PlatformTransactionManager txm, DataSource dataSource) {
        this.txm = txm;
        this.dataSource = dataSource;
    }

    @Override
    public Transaction beginTransaction() {
        return beginTransaction(null);
    }

    @Override
    public Transaction beginTransaction(TransactionDefinition td) {
        DefaultTransactionDefinition std = convert(td);

        org.springframework.transaction.TransactionStatus ts = txm.getTransaction(std);
        return new LeapTransaction(txm, ts).begin();
    }

    protected Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    protected void closeConnection(Connection connection) {
        if(null != connection) {
            Try.catchAll(() -> DataSourceUtils.releaseConnection(connection, dataSource));
        }
    }

    @Override
    protected AbstractTransaction getTransaction(boolean requiresNew) {
        return getTransaction(requiresNew ? REQUIRES_NEW : REQUIRED);
    }

    @Override
    protected AbstractTransaction getTransaction(TransactionDefinition td) {
        return getTransaction(convert(td));
    }

    private AbstractTransaction getTransaction(DefaultTransactionDefinition td) {
        org.springframework.transaction.TransactionStatus ts = txm.getTransaction(td);
        return new LeapTransaction(txm, ts);
    }

    private DefaultTransactionDefinition convert(TransactionDefinition td) {
        DefaultTransactionDefinition std;
        if(null == td) {
            std = new DefaultTransactionDefinition();
            std.setPropagationBehavior(td.getPropagation().getValue());
            std.setIsolationLevel(td.getIsolation().getValue());
        }else {
            std = new DefaultTransactionDefinition();
        }
        return std;
    }
}
