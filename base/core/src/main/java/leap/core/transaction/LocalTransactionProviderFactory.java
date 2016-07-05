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

import leap.core.transaction.TransactionDefinition.Isolation;
import leap.core.transaction.TransactionDefinition.Propagation;

import javax.sql.DataSource;

public class LocalTransactionProviderFactory implements TransactionProviderFactory {
	
	protected Propagation defaultPropagation = Propagation.REQUIRED;
	protected Isolation   defaultIsolation   = Isolation.DEFAULT;

	@Override
	public TransactionProvider getTransactionProvider(DataSource dataSource) {
		LocalTransactionProvider tm = new LocalTransactionProvider(dataSource);
		
		tm.setDefaultPropagation(defaultPropagation);
		tm.setDefaultIsolation(defaultIsolation);
		
		return tm;
	}

	public void setDefaultPropagation(Propagation defaultPropagation) {
		this.defaultPropagation = defaultPropagation;
	}

	public void setDefaultIsolation(Isolation defaultIsolation) {
		this.defaultIsolation = defaultIsolation;
	}

}