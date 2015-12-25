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

import javax.sql.DataSource;

import leap.core.transaction.TransactionDefinition.IsolationLevel;
import leap.core.transaction.TransactionDefinition.PropagationBehaviour;

public class DefaultTransactionManagerFactory implements TransactionManagerFactory {
	
	protected PropagationBehaviour defaultPropagationBehaviour = PropagationBehaviour.REQUIRED;
	protected IsolationLevel       defaultIsolotionLevel       = IsolationLevel.DEFAULT;

	@Override
	public TransactionManager createTransactionManager(DataSource dataSource) {
		DefaultTransactionManager tm = new DefaultTransactionManager(dataSource);
		
		tm.setDefaultPropagationBehaviour(defaultPropagationBehaviour);
		tm.setDefaultIsolotionLevel(defaultIsolotionLevel);
		
		return tm;
	}

	public void setDefaultPropagationBehaviour(PropagationBehaviour defaultPropagationBehaviour) {
		this.defaultPropagationBehaviour = defaultPropagationBehaviour;
	}

	public void setDefaultIsolotionLevel(IsolationLevel defaultIsolotionLevel) {
		this.defaultIsolotionLevel = defaultIsolotionLevel;
	}

}