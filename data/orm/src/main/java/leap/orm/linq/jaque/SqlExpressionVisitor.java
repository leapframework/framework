package leap.orm.linq.jaque;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import leap.lang.Strings;
import leap.lang.exception.LambdaException;
import leap.orm.linq.jaque.SqlCondition.CapturedParameter;
import leap.orm.linq.jaque.SqlCondition.ConstantParameter;
import leap.orm.linq.jaque.SqlCondition.Node;
import leap.orm.linq.jaque.SqlCondition.Text;

final class SqlExpressionVisitor implements ExpressionVisitor<SqlExpressionVisitor> {
	
	private static final String[] 		     TYPE_OPS   = new String[ExpressionType.MaxExpressionTypeValue];
	private static final Map<String, String> METHOD_OPS = new HashMap<String, String>();
	
	static {
		TYPE_OPS[ExpressionType.LogicalAnd]         = "and";
		TYPE_OPS[ExpressionType.LogicalNot]         = "not";
		TYPE_OPS[ExpressionType.LogicalOr]          = "or";
		TYPE_OPS[ExpressionType.Equal]              = "=";
		TYPE_OPS[ExpressionType.NotEqual]           = "<>";
		TYPE_OPS[ExpressionType.GreaterThan]        = ">";
		TYPE_OPS[ExpressionType.LessThan]           = "<";
		TYPE_OPS[ExpressionType.GreaterThanOrEqual] = ">=";
		TYPE_OPS[ExpressionType.LessThanOrEqual]    = "<=";
		
		METHOD_OPS.put("equals", "=");
	}

	final Expression    lambda;
	final AtomicInteger args = new AtomicInteger();
	final List<Node>    nodes = new ArrayList<Node>();
	
	public SqlExpressionVisitor(Expression lambda) {
		this.lambda = lambda;
		this.lambda.accept(this);
	}
	
	public SqlCondition result() {
		return new SqlCondition(nodes.toArray(new Node[nodes.size()]));
	}

	@Override
	public SqlExpressionVisitor visit(BinaryExpression e) {
		Expression left  = e.getFirst();
		Expression right = e.getSecond();
		
		String leftColumn  = resolveColumnName(left); 
		String rightColumn = resolveColumnName(right);
		
		//Both are column or Both not column
		if((null != leftColumn && null != rightColumn) || (null == leftColumn && null == rightColumn)) {
			boolean quoted = e != lambda && isRelation(e);
			
			if(quoted) {
				append("(");
			}
			
			left.accept(this);
			op(e);
			right.accept(this);
			
			if(quoted) {
				append(")");
			}
		}else{
			if(null != leftColumn) {
				append(leftColumn);
				op(e);
				param(leftColumn, right);
			}else{
				param(rightColumn, left);
				op(e);
				append(rightColumn);
			}
		}
		
		return this;
	}
	
	@Override
	public SqlExpressionVisitor visit(InvocationExpression e) {
		InvocableExpression ie = e.getTarget();
		if(ie instanceof MemberExpression) {
			Expression owner  = ((MemberExpression)ie).getInstance();
			Member     member = ((MemberExpression)ie).getMember();
			
			if(member instanceof Method) {
				Method method = (Method)member;
				
				if(method.getParameters().length != 1) {
					throw notSupported(e);
				}
				
				String op = METHOD_OPS.get(method.getName());
				
				if(null != op) {
					
					if(!(owner instanceof InvocationExpression)) {
						throw notSupported(owner);
					}
					
					String columnName = resolveColumnName(owner);
					if(null == columnName) {
						throw notSupported(owner);
					}
					
					append(columnName);
					op(op);
					param(columnName,e.getArguments().get(0));
					return this;
				}
			}
		}
		
		return e.getTarget().accept(this);
	}

	@Override
	public SqlExpressionVisitor visit(ConstantExpression e) {
		return append(e.getValue().toString());
	}

	@Override
	public SqlExpressionVisitor visit(LambdaExpression<?> e) {
		throw notSupported(e);
	}

	@Override
	public SqlExpressionVisitor visit(MemberExpression e) {
		String name = e.getMember().getName();
		name = name.replaceAll("^(get|is)", "").toLowerCase();
		return append(name);
	}

	@Override
	public SqlExpressionVisitor visit(ParameterExpression e) {
		return this;
	}

	@Override
	public SqlExpressionVisitor visit(UnaryExpression e) {
		append(ExpressionType.toString(e.getExpressionType()));
		return e.getFirst().accept(this);
	}
	
	protected boolean isRelation(Expression e) {
		int type = e.getExpressionType();
		if(type == ExpressionType.LogicalAnd || type == ExpressionType.LogicalOr || type == ExpressionType.LogicalNot) {
			return true;
		}
		return false;
	}
	
	protected SqlExpressionVisitor append(String s) {
		if(nodes.size() > 0) {
			Node last = nodes.get(nodes.size() - 1);
			if(last instanceof Text) {
				((Text) last).append(s);
				return this;
			}
		}
		nodes.add(new Text().append(s));
		return this;
	}
	
	protected void op(Expression e) {
		String op = TYPE_OPS[e.getExpressionType()];
		
		if(null == op) {
			throw notSupported(e);
		}
		
		append(" ").append(op).append(" ");
	}
	
	protected void op(String op) {
		append(" ").append(op).append(" ");
	}
	
	protected void op(Expression e, String method) {
		String op = METHOD_OPS.get(method);
		
		if(null == op) {
			throw notSupported(e);
		}
		
		append(" ").append(op).append(" ");
	}
	
	protected SqlExpressionVisitor append(Node n) {
		nodes.add(n);
		return this;
	}
	
	protected String resolveColumnName(Expression e) {
		if(e instanceof MemberExpression) {
			MemberExpression m = (MemberExpression)e;
			
			Member member = m.getMember();
			
			if(member instanceof Field) {
				Field f = (Field)member;
				
				if(Modifier.isStatic(f.getModifiers())) {
					return null;
				}
				
				return f.getName();
			}
			
			if(member instanceof Method) {
				Method method = (Method)member;
				
				if(method.getName().startsWith("get")) {
					return Strings.lowerFirst(method.getName().substring(3));
				}else if(method.getName().startsWith("is")) {
					return Strings.lowerFirst(method.getName().substring(2));
				}
			}
			
			throw notSupported(e);
		}
		
		if(e instanceof InvocationExpression) {
			return resolveColumnName(((InvocationExpression) e).getTarget());
		}
		
		return null;
	}
	
	protected int pindex() {
		return args.incrementAndGet();
	}
	
	protected void param(String columnName, Expression e) {
		if(e instanceof ConstantExpression) {
			append(new ConstantParameter(pindex(), ((ConstantExpression) e).getValue()));
			return;
		}
		
		if(e instanceof ParameterExpression) {
			ParameterExpression p = (ParameterExpression)e;
			append(new CapturedParameter(pindex(), p.getIndex()));
			return;
		}
		
		if(e instanceof InvocationExpression) {
			InvocableExpression  ie = ((InvocationExpression) e).getTarget();
			if(ie instanceof MemberExpression) {
				if(((MemberExpression) ie).getMember() instanceof Field) {
					Field field = ((Field)((MemberExpression)ie).getMember());
					try {
                        append(new ConstantParameter(pindex(), field.get(null)));
                        return;
                    } catch (Exception e1) {
                    	throw new LambdaException("Error getting static field '" + field.getName() + "' in expr : " + e, e1);
                    }
				}
			}
		}
		
		throw notSupported(e);
	}
	
	private static LambdaException notSupported(Expression e) {
		throw new LambdaException("Unsupported lambda expression : " + e);
	}
}