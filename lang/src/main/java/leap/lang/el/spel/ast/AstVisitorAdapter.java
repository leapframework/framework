package leap.lang.el.spel.ast;


public class AstVisitorAdapter implements AstVisitor {
	
	@Override
	public void preVisit(AstNode node) {
		
	}

	@Override
	public void postVisit(AstNode node) {
		
	}
	
	@Override
	public boolean startVisit(AstBinary node) {
		return true;
	}

	@Override
	public void endVisit(AstBinary node) {
		
	}
	
	@Override
	public boolean startVisit(AstIdentifier node) {
		return true;
	}

	@Override
	public void endVisit(AstIdentifier node) {
		
	}

	@Override
	public void endVisit(AstNull node) {
		
	}

	@Override
	public boolean startVisit(AstNull node) {
		return true;
	}

	@Override
	public void endVisit(AstProperty node) {
		
	}

	@Override
	public boolean startVisit(AstProperty node) {
		return true;
	}

	@Override
	public void endVisit(AstMethod node) {
		
	}

	@Override
	public boolean startVisit(AstMethod node) {
		return true;
	}
	
	@Override
    public void endVisit(AstFunction node) {
	    
    }

	@Override
    public boolean startVisit(AstFunction node) {
	    return true;
    }

	@Override
	public void endVisit(AstNumber node) {
		
	}

	@Override
	public boolean startVisit(AstNumber node) {
		return true;
	}

	@Override
	public void endVisit(AstString node) {
		
	}

	@Override
	public boolean startVisit(AstString node) {
		return true;
	}

	@Override
	public void endVisit(AstBoolean node) {
		
	}

	@Override
	public boolean startVisit(AstBoolean node) {
		return true;
	}
	
	@Override
	public void endVisit(AstItem node) {
		
	}
	
	@Override
	public boolean startVisit(AstItem node) {
		return true;
	}
	
	@Override
	public void endVisit(AstChoice node) {
		
	}
	
	@Override
	public boolean startVisit(AstChoice node) {
		return true;
	}
	
	@Override
	public void endVisit(AstUnary node) {
		
	}
	
	@Override
	public boolean startVisit(AstUnary node) {
		return true;
	}
}
