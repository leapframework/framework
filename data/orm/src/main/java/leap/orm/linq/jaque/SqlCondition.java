/*
 * Copyright 2014 the original author or authors.
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
package leap.orm.linq.jaque;

import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.util.concurrent.atomic.AtomicInteger;

import leap.lang.params.Params;

final class SqlCondition {
	private final Node[]  nodes;
	private final boolean captured;

	public SqlCondition(Node[] nodes) {
		this.nodes    = nodes;
		this.captured = evalCaptured();
	}
	
	public final boolean hasCapturedParameters() {
		return captured;
	}
	
	public final String toSql() {
		StringBuilder sb = new StringBuilder();
		for(Node node : nodes) {
			node.toSql(sb);
		}
		return sb.toString();
	}
	
	public final String toSql(SerializedLambda sl,Params params,AtomicInteger paramsCounter) {
		StringBuilder sb = new StringBuilder();
		for(Node node : nodes) {
			node.toSql(sb, sl, params, paramsCounter);
		}
		return sb.toString();
	}
	
	protected final boolean evalCaptured() {
		for(Node node : nodes){
			if(node instanceof CapturedParameter) {
				return true;
			}
		}
		return false;
	}
	
	protected static abstract class Node {
		
		public final void toSql(Appendable out) {
			try {
	            _toSql(out);
            } catch (IOException e) {
            	throw new IllegalStateException("Unexpected error," + e.getMessage(), e);
            }
		}
		
		public final void toSql(Appendable out, SerializedLambda sl, Params params, AtomicInteger paramsCounter) {
			try {
	            _toSql(out, sl, params, paramsCounter);
            } catch (IOException e) {
            	throw new IllegalStateException("Unexpected error," + e.getMessage(), e);
            }
		}
		
		protected void _toSql(Appendable out, SerializedLambda sl, Params params, AtomicInteger paramsCounter) throws IOException {
			_toSql(out);
		}
		
		protected abstract void _toSql(Appendable out) throws IOException;
	}
	
	protected static final class Text extends Node {
		
		private final StringBuilder sb = new StringBuilder();

		public Text append(String s) {
			sb.append(s);
			return this;
		}
		
		@Override
        protected void _toSql(Appendable out) throws IOException {
	        out.append(sb);
        }
	}
	
	protected static abstract class Parameter extends Node {
		
		protected final int    index;
		protected final String name;

		public Parameter(int index) {
			this.index = index;
			this.name  = "p" + index;
		}
		
	}

	protected static final class ConstantParameter extends Parameter {
		
		private final Object value;
		
		public ConstantParameter(int index, Object value) {
			super(index);
			this.value = value;
		}

		@Override
        protected void _toSql(Appendable out) throws IOException {
			out.append(':').append(name);   
        }

		@Override
        protected void _toSql(Appendable out, SerializedLambda sl, Params params, AtomicInteger paramsCounter) throws IOException {
			String name = "p" + paramsCounter.incrementAndGet();
			out.append(':').append(name);
			params.set(name, value);
        }
	}
	
	protected static final class CapturedParameter extends Parameter {
		private final int capturedIndex;
		
		public CapturedParameter(int index,int capturedIndex) {
			super(index);
			this.capturedIndex = capturedIndex;
		}
		
		@Override
        protected void _toSql(Appendable out) throws IOException {
			out.append(':').append(name);
        }

		@Override
        protected void _toSql(Appendable out, SerializedLambda sl, Params params, AtomicInteger paramsCounter) throws IOException {
			String name = "p" + paramsCounter.incrementAndGet();
			out.append(':').append(name);
			params.set(name, sl.getCapturedArg(capturedIndex));
        }
	}
}