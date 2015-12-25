package leap.lang.el.spel.ast;


public interface AstVisitor {
	void preVisit(AstNode node);
    void postVisit(AstNode node);

    boolean startVisit(AstBinary node);
    void endVisit(AstBinary node);
    
    boolean startVisit(AstIdentifier node);
    void endVisit(AstIdentifier node);
    
    boolean startVisit(AstNull node);
    void endVisit(AstNull node);
    
    boolean startVisit(AstProperty node);
    void endVisit(AstProperty node);
    
    boolean startVisit(AstMethod node);
    void endVisit(AstMethod node);
    
    boolean startVisit(AstFunction node);
    void endVisit(AstFunction node);
    
    boolean startVisit(AstNumber node);
    void endVisit(AstNumber node);
    
    boolean startVisit(AstString node);
    void endVisit(AstString node);
    
    boolean startVisit(AstBoolean node);
    void endVisit(AstBoolean node);
    
    boolean startVisit(AstItem node);
    void endVisit(AstItem node);
    
    boolean startVisit(AstChoice node);
    void endVisit(AstChoice node);
    
    boolean startVisit(AstUnary node);
    void endVisit(AstUnary node);
    
    default boolean startVisit(AstType node) {
    	return true;
    }
    
    default void endVisit(AstType node) {
    	
    }
}
