/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.core.transaction;

import leap.core.AppContext;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;

import javax.sql.DataSource;

/**
 * Transaction management utils.
 */
public class Transactions {
	
	public static void execute(DataSource dataSource, ConnectionCallback callback) throws NestedSQLException {
        tm().execute(dataSource, callback);
	}
	
	public static <T> T execute(DataSource dataSource, ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
        return tm().execute(dataSource, callback);
	}

    protected static TransactionManager tm() {
        return AppContext.factory().getBean(TransactionManager.class);
    }

	protected Transactions(){
		
	}

}