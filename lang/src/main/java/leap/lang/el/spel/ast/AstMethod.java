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

import java.io.Serializable;

import leap.lang.el.ElEvalContext;
import leap.lang.el.ElException;
import leap.lang.el.ElMethod;
import leap.lang.el.ElMethodInvocable;


public class AstMethod extends AstInvocable implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private AstExpr  owner;
    
    private Class<?> c;
    private ElMethod m;

    public AstMethod() {

    }

    public AstMethod(AstExpr owner, String name, AstExpr[] parameters) {
    	super(name,parameters);
        this.owner = owner;
    }

    public AstExpr getOwner() {
        return this.owner;
    }

    public void setOwner(AstExpr owner) {
        this.owner = owner;
    }
    
    @Override
    public Object eval(ElEvalContext context) {
    	Object o = owner.eval(context);
    	
    	if(null == o){
    		return null;
    	}
    	
    	if(o instanceof ElMethodInvocable){
    		try {
	            return ((ElMethodInvocable) o).invokeMethod(context, name, evalParameters(context));
            } catch (Throwable e) {
            	throw new ElException(context.getMessage("el.errors.methodInvokeError", name, o, e.getMessage()),e);
            }
    	}
    	
    	Object[] args = evalParameters(context);
    	
    	if(null == m){
    		synchronized (this) {
	            if(null == m){
	            	if(o instanceof Class<?>){
	            		this.c = (Class<?>)o;
	            		this.m = resolveMethod(context, c, args);
	            	}else{
	            		this.c = o.getClass();
	            		this.m = resolveMethod(context, o, c, args);
	            	}
	            }
            }
    	}
    	
    	ElMethod m = this.m;
    	Class<?> c = AstProperty.cls(o);
    	if(this.c != c){
    		if(c == o){
    			m = resolveMethod(context, c, args);	
    		}else{
    			m = resolveMethod(context, o, c, args);
    		}
    	}
    	
    	try {
	        return m.invoke(context,o,args);
        } catch (Throwable e) {
        	throw new ElException(context.getMessage("el.errors.methodInvokeError", name, o, e.getMessage()),e);
        }
    }
    
    protected ElMethod resolveMethod(ElEvalContext ctx,Class<?> c,Object[] args) {
    	ElMethod m = ctx.resolveMethod(c, name, args);
    	if(null == m){
    		throw new ElException(ctx.getMessage("el.errors.noSuchMethod",name,c));
    	}
    	return m;
    }
    
    protected ElMethod resolveMethod(ElEvalContext ctx,Object owner, Class<?> c, Object[] args) {
    	ElMethod m = ctx.resolveMethod(owner, c, name, args);
    	if(null == m){
    		throw new ElException(ctx.getMessage("el.errors.noSuchMethod",name,c));
    	}
    	return m;
    }
    
	@Override
    protected void doAccept(AstVisitor visitor) {
        if (visitor.startVisit(this)) {
        	acceptChild(visitor,  this.owner);
            acceptChilds(visitor, this.parameters);
        }
        visitor.endVisit(this);
    }
    
    public void toString(StringBuilder buf) {
    	owner.toString(buf);
    	buf.append(".");
    	super.toString(buf);
    }
}
