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

public class AstBinary extends AstExpr {
	
	public static interface BOperator {
		
		String getLiteral();
		
		int getPriority();
		
		Class<?> getResultType(int ltype, Class<?> lcls, int rtype, Class<?> rcls);
		
		Object eval(ElEvalContext context,int ltype, AstNode left, int rtype, AstNode right);
	}
	
	public static abstract class AbstractOperator implements BOperator {
		protected final String literal;
		protected final int    priority;

		public AbstractOperator(String literal,int priority) {
			this.literal  = literal;
			this.priority = priority;
		}
		
		@Override
        public String getLiteral() {
	        return literal;
        }

		@Override
        public int getPriority() {
	        return priority;
        }

		@Override
        public String toString() {
			return literal;
		}

		@Override
        public Class<?> getResultType(int ltype, Class<?> lcls, int rtype, Class<?> rcls) {
	        return null;
        }
	}
	
	protected static abstract class SimpleOperator extends AbstractOperator{
		public SimpleOperator(String literal, int priority) {
	        super(literal,priority);
        }

		@Override
        public final Object eval(ElEvalContext context, int ltype, AstNode left, int rtype, AstNode right) {
	        return apply(context, ltype, left.eval(context), rtype, right.eval(context));
        }
		
		protected abstract Object apply(ElEvalContext context,int ltype, Object left, int rtype, Object right);
	}
	
	public static final BOperator ADD = new SimpleOperator("+",70) {

		@Override
        public Class<?> getResultType(int ltype, Class<?> lcls, int rtype, Class<?> rcls) {
			if(ltype == ElTypes.STRING || rtype == ElTypes.STRING){
				return String.class;
			}
	        return null;
        }

		@Override
        protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
	        return Ops.add(context, ltype, left, rtype, right);
        }
	};
	
	public static final BOperator SUB = new SimpleOperator("-",70) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			return Ops.sub(context, ltype, left, rtype, right);
		}
	};
	
	public static final BOperator MUL = new SimpleOperator("*",60) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			return Ops.mul(context, ltype, left, rtype, right);
		}
	};
	
	public static final BOperator DIV = new SimpleOperator("/",60) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			return Ops.div(context, ltype, left, rtype, right);
		}
	};
	
	public static final BOperator MOD = new SimpleOperator("%",60) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			return Ops.mod(context, ltype, left, rtype, right);
		}
	};
	
	public static final BOperator AND = new AbstractOperator("&&",140) {
		@Override
		public Object eval(ElEvalContext context, int ltype, AstNode left, int rtype, AstNode right) {
			Object lval = left.eval(context);
			if(!context.test(lval)) {
				return Boolean.FALSE;
			}
			return context.test(right.eval(context));
		}
	};
	
	public static final BOperator OR = new AbstractOperator("||",160) {
		@Override
        public Object eval(ElEvalContext context, int ltype, AstNode left, int rtype, AstNode right) {
			Object lval = left.eval(context);
			if(context.test(lval)){
				return Boolean.TRUE;
			}
	        return context.test(right.eval(context));
        }
	};
	
	public static abstract class EqOperatorBase extends AbstractOperator {

		public EqOperatorBase(String literal, int priority) {
	        super(literal, priority);
        }

		@Override
        public Object eval(ElEvalContext context, int ltype, AstNode left, int rtype, AstNode right) {
			Object lval = left.eval(context);
			Object rval = right.eval(context);
			
			if(lval == rval){
				return Boolean.TRUE;
			}
			
			if(lval == null || rval == null){
				return Boolean.FALSE;
			}
			
			return apply(context, ltype, lval, rtype, rval);
        }
		
		protected abstract Object apply(ElEvalContext context,int ltype, Object lval, int rtype, Object rval);
	}
	
	public static final BOperator EQ = new EqOperatorBase("==",170) {
		@Override
        protected Object apply(ElEvalContext context, int ltype, Object lval, int rtype, Object rval) {
			return Ops.eq(context, ltype, lval, rtype, rval);
        }
	};
	
	public static final BOperator GE = new EqOperatorBase(">=",110) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object lval, int rtype, Object rval) {
			return Ops.ge(context, ltype, lval, rtype, rval);
		}
	};
	
	public static final BOperator LE = new EqOperatorBase("<=",110) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object lval, int rtype, Object rval) {
			return Ops.le(context, ltype, lval, rtype, rval);
		}
	};
	
	public static final BOperator GT = new AbstractOperator(">",110) {

		@Override
        public Object eval(ElEvalContext ctx, int ltype, AstNode left, int rtype, AstNode right) {
			Object lval = left.eval(ctx);
			if(null == lval){
				return Boolean.FALSE;
			}
			
			Object rval = right.eval(ctx);
			if(null == rval){
				return Boolean.FALSE;
			}
			
			if(lval == rval){
				return Boolean.FALSE;
			}
			
			return Ops.gt(ctx, ltype, lval, rtype, rval);
        }
	};
	
	public static final BOperator LT = new AbstractOperator("<",110) {
		@Override
        public Object eval(ElEvalContext ctx, int ltype, AstNode left, int rtype, AstNode right) {
			Object lval = left.eval(ctx);
			if(null == lval){
				return Boolean.FALSE;
			}
			
			Object rval = right.eval(ctx);
			if(null == rval){
				return Boolean.FALSE;
			}
			
			if(lval == rval){
				return Boolean.FALSE;
			}
			
			return Ops.lt(ctx, ltype, lval, rtype, rval);
        }
	};
	
	public static final BOperator NE = new AbstractOperator("!=",110) {

		@Override
        public Object eval(ElEvalContext context, int ltype, AstNode left, int rtype, AstNode right) {
			Object lval = left.eval(context);
			Object rval = right.eval(context);
			
			if(lval == rval){
				return false;
			}
			
			if(lval == null || rval == null){
				return true;
			}
			
			return Boolean.FALSE == Ops.eq(context, ltype, lval, rtype, rval);
        }
	};
	
	public static final BOperator INSTANCE_OF = new SimpleOperator("instanceof",110) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'instanceof' operator not supported now");
		}
	};
	
	public static final BOperator BIT_AND = new SimpleOperator("&",90) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'&' operator not supported now");
		}
	};
	
	public static final BOperator BIT_OR = new SimpleOperator("|",100) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'|' operator not supported now");
		}
	};
	
	public static final BOperator BIT_XOR = new SimpleOperator("^",50) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'^' operator not supported now");
		}
	};
	
	public static final BOperator ASSIGN = new SimpleOperator("=",169) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'=' operator not supported now");
		}
	};
	
	public static final BOperator MUL_ASSIGN = new SimpleOperator("*=",169) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'*=' operator not supported now");
		}
	};
	
	public static final BOperator DIV_ASSIGN = new SimpleOperator("/=",169) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'/=' operator not supported now");
		}
	};
	
	public static final BOperator ADD_ASSIGN = new SimpleOperator("+=",169) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'+=' operator not supported now");
		}
	};
	
	public static final BOperator SUB_ASSIGN = new SimpleOperator("-=",169) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'-=' operator not supported now");
		}
	};	
	
	public static final BOperator LSHIFT = new SimpleOperator("<<",80) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'<<' operator not supported now");
		}
	};
	
	public static final BOperator RSHIFT = new SimpleOperator(">>",80) {
		@Override
		protected Object apply(ElEvalContext context, int ltype, Object left, int rtype, Object right) {
			throw new IllegalStateException("'>>' operator not supported now");
		}
	};
	
	private AstExpr   left;
	private AstExpr   right;
	private int	      ltype = -1;
	private int	      rtype = -1;
	private BOperator operator;

	public AstBinary() {

	}

	public AstBinary(AstExpr left, BOperator operator, AstExpr right) {
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.ltype = ElTypes.resolveType(left.resultType);
		this.rtype = ElTypes.resolveType(right.resultType);
		this.resultType = operator.getResultType(ltype, left.resultType, rtype, right.resultType);
	}

	public AstExpr getLeft() {
		return this.left;
	}

	public void setLeft(AstExpr left) {
		this.left = left;
	}

	public AstExpr getRight() {
		return this.right;
	}

	public void setRight(AstExpr right) {
		this.right = right;
	}

	public BOperator getOperator() {
		return this.operator;
	}

	public void setOperator(BOperator operator) {
		this.operator = operator;
	}

	@Override
	public Object eval(ElEvalContext context) {
		return operator.eval(context, ltype, left, rtype, right);
	}

	protected void doAccept(AstVisitor visitor) {
		if (visitor.startVisit(this)) {
			acceptChild(visitor, this.left);
			acceptChild(visitor, this.right);
		}
		visitor.endVisit(this);
	}

	public void toString(StringBuilder buf) {
		this.left.toString(buf);
		buf.append(" ");
		buf.append(this.operator);
		buf.append(" ");
		this.right.toString(buf);
	}
}
