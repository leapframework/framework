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

import leap.core.transaction.TransactionDefinition.IsolationLevel;
import leap.core.transaction.TransactionDefinition.PropagationBehaviour;

import javax.sql.DataSource;

public class LocalTransactionProviderFactory implements TransactionProviderFactory {
	
	protected PropagationBehaviour defaultPropagationBehaviour = PropagationBehaviour.REQUIRED;
	protected IsolationLevel       defaultIsolationLevel       = IsolationLevel.DEFAULT;

	@Override
	public TransactionProvider getTransactionProvider(DataSource dataSource) {
		LocalTransactionProvider tm = new LocalTransactionProvider(dataSource);
		
		tm.setDefaultPropagationBehaviour(defaultPropagationBehaviour);
		tm.setDefaultIsolationLevel(defaultIsolationLevel);
		
		return tm;
	}

	public void setDefaultPropagationBehaviour(PropagationBehaviour defaultPropagationBehaviour) {
		this.defaultPropagationBehaviour = defaultPropagationBehaviour;
	}

	public void setDefaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
		this.defaultIsolationLevel = defaultIsolationLevel;
	}

}