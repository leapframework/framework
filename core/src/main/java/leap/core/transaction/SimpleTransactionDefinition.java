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

import java.io.Serializable;

import leap.lang.Args;

//from spring framework
public class SimpleTransactionDefinition implements TransactionDefinition, Serializable {
	
	private static final long serialVersionUID = 6664083840731175152L;

	/** Prefix for the propagation constants defined in TransactionDefinition */
	private static final String PREFIX_PROPAGATION = "PROPAGATION_";

	/** Prefix for the isolation constants defined in TransactionDefinition */
	private static final String PREFIX_ISOLATION = "ISOLATION_";

	private Propagation propagationBehavior = Propagation.REQUIRED;

	private Isolation isolation = Isolation.DEFAULT;

	/**
	 * Create a new DefaultTransactionDefinition, with default settings.
	 * Can be modified through bean property setters.
	 * @see #setPropagationBehavior
	 * @see #setIsolation
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public SimpleTransactionDefinition() {
		
	}

	/**
	 * Copy constructor. Definition can be modified through bean property setters.
	 * @see #setPropagationBehavior
	 * @see #setIsolation
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public SimpleTransactionDefinition(TransactionDefinition other) {
		this.propagationBehavior = other.getPropagationBehavior();
		this.isolation = other.getIsolation();
	}
	
	public SimpleTransactionDefinition(Propagation propagationBehavior) {
		Args.notNull(propagationBehavior,"propagationBehavior");
		this.propagationBehavior = propagationBehavior;
	}
	
	public final Propagation getPropagationBehavior() {
		return this.propagationBehavior;
	}

	public final Isolation getIsolation() {
		return this.isolation;
	}
	
	public void setPropagationBehavior(Propagation propagationBehavior) {
		Args.notNull(propagationBehavior,"propagationBehavior");
		this.propagationBehavior = propagationBehavior;
	}

	public void setIsolation(Isolation isolation) {
		Args.notNull(isolation,"isolationLevel");
		this.isolation = isolation;
	}

	/**
	 * This implementation compares the {@code toString()} results.
	 * @see #toString()
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof TransactionDefinition && toString().equals(other.toString()));
	}

	/**
	 * This implementation returns {@code toString()}'s hash code.
	 * @see #toString()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return getDefinitionDescription().toString();
	}

	/**
	 * Return an identifying description for this transaction definition.
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 */
	protected final StringBuilder getDefinitionDescription() {
		StringBuilder result = new StringBuilder();
		result.append(PREFIX_PROPAGATION + this.propagationBehavior.name());
		result.append(',');
		result.append(PREFIX_ISOLATION + this.isolation.name());
		return result;
	}
}