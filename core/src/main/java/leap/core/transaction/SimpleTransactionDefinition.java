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

import leap.lang.Args;

import java.io.Serializable;

public class SimpleTransactionDefinition implements TransactionDefinition, Serializable {
	
	private static final long serialVersionUID = 6664083840731175152L;

    protected Propagation propagation = Propagation.REQUIRED;
    protected Isolation   isolation   = Isolation.DEFAULT;

	public final Propagation getPropagation() {
		return this.propagation;
	}

	public final Isolation getIsolation() {
		return this.isolation;
	}
	
	public void setPropagation(Propagation propagation) {
		Args.notNull(propagation,"propagation");
		this.propagation = propagation;
	}

	public void setIsolation(Isolation isolation) {
		Args.notNull(isolation,"isolation");
		this.isolation = isolation;
	}

	@Override
	public String toString() {
		return "{propagation:" + propagation + ", isolation:" + isolation + "}";
	}

}