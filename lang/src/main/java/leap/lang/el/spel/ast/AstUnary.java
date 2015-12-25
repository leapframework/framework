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
package leap.lang.el.spel.ast;

import leap.lang.el.ElEvalContext;
import leap.lang.el.ElTypes;

public class AstUnary extends AstExpr {
	public static interface UOperator {
		
		boolean isPrepositive();
		
		String getLiteral();
		
		Class<?> getResultType(int type, Class<?> cls);
		
		Object eval(ElEvalContext context, int type, AstNode expr);
	}
	
	public static abstract class AbstractOperator implements UOperator {
		protected final String literal;

		public AbstractOperator(String literal) {
			this.literal = literal;
		}
		
		@Override
        public boolean isPrepositive() {
	        return true;
        }

		@Override
        public String getLiteral() {
	        return literal;
        }

		@Override
        public String toString() {
			return literal;
		}

		@Override
        public Class<?> getResultType(int type, Class<?> cls) {
	        return null;
        }
	}
	
	public static final UOperator NOT = new AbstractOperator("!") {
		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			return !context.test(expr.eval(context));
		}
	};
	
	public static final UOperator PLUS = new AbstractOperator("+") {
		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			return expr.eval(context);
		}
	};
	
	public static final UOperator MINUS = new AbstractOperator("-") {
		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			return Ops.minus(context, type, expr.eval(context));
		}
	};
	
	public static final UOperator PRE_PLUSPLUS = new AbstractOperator("++") {
		
		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			throw new IllegalStateException("++ operator not supported now");
		}
	};
	
	public static final UOperator POST_PLUSPLUS = new AbstractOperator("++") {
		@Override
        public boolean isPrepositive() {
			return false;
		}

		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			throw new IllegalStateException("++ operator not supported now");
		}
	};
	
	public static final UOperator PRE_SUBSUB = new AbstractOperator("--") {
		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			throw new IllegalStateException("-- operator not supported now");
		}
	};
	
	public static final UOperator POST_SUBSUB = new AbstractOperator("--") {
		@Override
        public boolean isPrepositive() {
			return false;
		}
		
		@Override
		public Object eval(ElEvalContext context, int type, AstNode expr) {
			throw new IllegalStateException("-- operator not supported now");
		}
	};
	
	private AstExpr   expr;
	private int       type;
	private UOperator operator;

	public AstUnary() {

	}

	public AstUnary(AstExpr expr, UOperator operator) {
		super();
		this.expr     = expr;
		this.type     = ElTypes.resolveType(expr.resultType);
		this.operator = operator;
	}

	public AstExpr getExpr() {
		return expr;
	}

	public void setExpr(AstExpr expr) {
		this.expr = expr;
	}

	public UOperator getOperator() {
		return operator;
	}

	public void setOperator(UOperator operator) {
		this.operator = operator;
	}

	@Override
    public Object eval(ElEvalContext context) {
	    return operator.eval(context, type, expr);
    }

	@Override
	protected void doAccept(AstVisitor visitor) {
		if (visitor.startVisit(this)) {
			acceptChild(visitor, expr);
		}
		visitor.endVisit(this);
	}
}
