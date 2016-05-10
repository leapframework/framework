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

package leap.core.transaction;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceListener;
import leap.core.ds.DataSourceManager;
import leap.lang.Initializable;
import leap.lang.jdbc.ConnectionCallback;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTransactionManager implements TransactionManager, DataSourceListener, Initializable {

    protected @Inject BeanFactory                factory;
    protected @Inject DataSourceManager          dsm;
    protected @Inject TransactionProviderFactory tpf;

    protected Map<DataSource, TransactionProvider> providers = new ConcurrentHashMap<>(2);

    protected static class ClosableTransactionImpl implements ClosableTransaction {
        @Override
        public void close() {
            System.out.println("close transaction");
        }
    }

    @Override
    public ClosableTransaction begin() {
        System.out.println("begin transaction");
        return new ClosableTransactionImpl();
    }

    @Override
    public void execute(ConnectionCallback callback) {
        execute(dsm.getDefaultDataSource(), callback);
    }

    @Override
    public void doTransaction(TransactionCallback callback) {
        doTransaction(dsm.getDefaultDataSource(), callback);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
        return doTransaction(dsm.getDefaultDataSource(), callback);
    }

    @Override
    public void doTransaction(TransactionCallback callback, boolean requiresNew) {
        doTransaction(dsm.getDefaultDataSource(), callback, requiresNew);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
        return doTransaction(dsm.getDefaultDataSource(), callback, requiresNew);
    }

    @Override
    public void execute(DataSource ds, ConnectionCallback callback) {

    }

    @Override
    public void doTransaction(DataSource ds, TransactionCallback callback) {

    }

    @Override
    public <T> T doTransaction(DataSource ds, TransactionCallbackWithResult<T> callback) {
        return null;
    }

    @Override
    public void doTransaction(DataSource ds, TransactionCallback callback, boolean requiresNew) {

    }

    @Override
    public <T> T doTransaction(DataSource ds, TransactionCallbackWithResult<T> callback, boolean requiresNew) {
        return null;
    }

    @Override
    public void onDataSourceCreated(String name, DataSource ds) {
        loadProvider(name, ds);
    }

    @Override
    public void onDataSourceDestroyed(String name, DataSource ds) {
        providers.remove(ds);
    }

    @Override
    public void init() {
        dsm.getAllDataSources().entrySet().forEach((entry) -> loadProvider(entry.getKey(), entry.getValue()));
    }
    
    protected TransactionProvider getTransactionProvider(DataSource ds) {
        TransactionProvider tp = providers.get(ds);
        if(null == tp) {
            throw new IllegalStateException("No Transaction Provider for data source '" + ds + "'");
        }
        return tp;
    }

    protected void loadProvider(String name, DataSource ds) {
        TransactionProvider tp = factory.tryGetBean(TransactionProvider.class, name);
        if(null == tp) {
            tp = tpf.getTransactionProvider(ds);
        }
        providers.put(ds, tp);
    }
}