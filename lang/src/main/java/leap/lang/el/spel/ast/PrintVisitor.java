package leap.lang.el.spel.ast;

import static leap.lang.el.spel.ast.AstBinary.ADD;
import static leap.lang.el.spel.ast.AstBinary.DIV;
import static leap.lang.el.spel.ast.AstBinary.EQ;
import static leap.lang.el.spel.ast.AstBinary.GE;
import static leap.lang.el.spel.ast.AstBinary.GT;
import static leap.lang.el.spel.ast.AstBinary.LE;
import static leap.lang.el.spel.ast.AstBinary.LT;
import static leap.lang.el.spel.ast.AstBinary.MUL;
import static leap.lang.el.spel.ast.AstBinary.SUB;

import java.io.PrintWriter;

import leap.lang.el.spel.ast.AstBinary.BOperator;

public class PrintVisitor extends AstVisitorAdapter {
	protected PrintWriter out;
	private String indent = "\t";
	private int indentCount = 0;

	public PrintVisitor(PrintWriter out) {
		this.out = out;
	}

	public void decrementIndent() {
		this.indentCount -= 1;
	}

	public void incrementIndent() {
		this.indentCount += 1;
	}

	public void printIndent() {
		for (int i = 0; i < this.indentCount; ++i)
			print(this.indent);
	}

	public void println() {
		print("\n");
		printIndent();
	}

	public void println(String text) {
		print(text);
		println();
	}

	public void print(String text) {
		out.print(text);
	}

	public void print(char ch) {
		out.print(ch);
	}

	@Override
	public boolean startVisit(AstBinary x) {
		BOperator op = x.getOperator();
		
        if(op == ADD){
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == MUL){
            print(" _multi(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == DIV){
            print(" _div(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == SUB){
            print(" _sub(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == GT){
            print(" _gt(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == GE){
            print(" _gteq(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == LT){
            print(" _lt(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == LE){
            print(" _lteq(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
        
        if(op == EQ){
            print(" _eq(");
            x.getLeft().accept(this);
            print(", ");
            x.getRight().accept(this);
            print(")");
            return false;
        }
	    
		if (x.getLeft() instanceof AstBinary) {
			AstBinary left = (AstBinary) x.getLeft();
			if (left.getOperator().getPriority() > x.getOperator().getPriority()) {
				print('(');
				left.accept(this);
				print(')');
			} else {
				left.accept(this);
			}
		} else {
			x.getLeft().accept(this);
		}

		print(" ");
		print(x.getOperator().getLiteral());
		print(" ");

		if (x.getRight() instanceof AstBinary) {
			AstBinary right = (AstBinary) x.getRight();
			if (right.getOperator().getPriority() >= x.getOperator().getPriority()) {
				print('(');
				right.accept(this);
				print(')');
			} else {
				right.accept(this);
			}
		} else {
			x.getRight().accept(this);
		}

		return false;
	}

	@Override
	public boolean startVisit(AstIdentifier x) {
		print(x.getName());
		return false;
	}

	@Override
	public boolean startVisit(AstNull x) {
		print("null");
		return false;
	}

	@Override
	public boolean startVisit(AstProperty x) {
		if (x.getOwner() instanceof AstBinary) {
			print('(');
			x.getOwner().accept(this);
			print(')');
		} else {
			x.getOwner().accept(this);
		}

		print(".");
		print(x.getName());
		return false;
	}

	@Override
	public boolean startVisit(AstMethod x) {
		if (x.getOwner() != null) {
			if (x.getOwner() instanceof AstBinary) {
				print('(');
				x.getOwner().accept(this);
				print(')');
			} else {
				x.getOwner().accept(this);
			}

			print(".");
		}
		print(x.getName());
		print("(");
		printAndAccept(x.getParameters(), ", ");
		print(")");
		return false;
	}

	@Override
	public boolean startVisit(AstNumber x) {
		Number value = x.getValue();

		if (value == null) {
			print("null");
			return false;
		}

		print(x.getValue().toString());
		return false;
	}

	@Override
	public boolean startVisit(AstChoice x) {
		x.getQuestion().accept(this);
		print(" ? ");
		x.getYes().accept(this);
		print(" : ");
		x.getNo().accept(this);
		return false;
	}

	@Override
	public boolean startVisit(AstBoolean x) {
		if (x.getValue()) {
			print("true");
		} else {
			print("false");
		}
		return false;
	}

	@Override
	public boolean startVisit(AstItem x) {
		x.getArray().accept(this);
		print("[");
		x.getIndex().accept(this);
		print("]");
		return false;
	}

	@Override
	public boolean startVisit(AstString x) {
		String value = x.getValue();
		if (value == null) {
			print("null");
		} else {
			print('"');
			for (char ch : value.toCharArray()) {
				switch (ch) {
				case '\t':
					print("\\t");
					break;
				case '\n':
					print("\\n");
					break;
				case '\r':
					print("\\r");
					break;
				case '\f':
					print("\\f");
					break;
				case '\b':
					print("\\b");
					break;
				case '\"':
					print("\\\"");
					break;
				default:
					print(ch);
				}
			}
			print('"');
		}

		return false;
	}

	@Override
	public boolean startVisit(AstUnary x) {
		leap.lang.el.spel.ast.AstUnary.UOperator o = x.getOperator();
		
		if(o == AstUnary.NOT){
		    if (x.getExpr() instanceof AstBinary) {
		        print("(");
		        x.getExpr().accept(this);
		        print(")");
		    } else {
		        x.getExpr().accept(this);		        
		    }
			return false;
		}
		
		if(o.isPrepositive()){
			print(o.getLiteral());
			x.getExpr().accept(this);
		}else{
			x.getExpr().accept(this);
			print(o.getLiteral());
		}
		
		return false;
	}

	protected void printAndAccept(AstNode[] nodes, String seperator) {
		for (int i = 0, size = nodes.length; i < size; ++i) {
			if (i != 0) {
				print(seperator);
			}
			nodes[i].accept(this);
		}
	}
}
