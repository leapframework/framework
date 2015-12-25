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

import java.util.Map;

import leap.lang.Named;
import leap.lang.beans.DynaBean;
import leap.lang.el.ElEvalContext;
import leap.lang.el.ElException;
import leap.lang.el.ElParseContext;
import leap.lang.el.ElProperty;
import leap.lang.el.ElPropertyResolver;


public class AstProperty extends AstExpr implements Named {

    private AstExpr owner;
    private String  name;
    private String  qName;
    
    private Class<?>   c;
    private ElProperty p;
    
    public AstProperty() {

    }
    
    public AstProperty(AstExpr owner, String name) {
        this.owner = owner;
        this.name  = name;
    }
    
    public AstProperty(ElParseContext context, AstExpr owner, String name) {
        this(owner, name);
    }
    
    public AstExpr getOwner() {
        return this.owner;
    }

    public void setOwner(AstExpr owner) {
        this.owner = owner;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object eval(ElEvalContext context) {
        if(null == qName) {
            qName = owner.toString() + "." + name;
            Object o = context.resolveVariable(qName);
            if(null != o) {
                return o;
            }
        }else{
            Object o = context.resolveVariable(qName);
            if(null != o) {
                return o;
            }
        }
        
    	Object o = owner.eval(context);
    	
    	if(null == o){
    		return null;
    	}
    	
    	if(o instanceof Map){
    		return ((Map)o).get(name);
    	}
    	
    	if(o instanceof ElPropertyResolver){
    		return ((ElPropertyResolver) o).resovleProperty(name);
    	}
    	
    	if(o instanceof DynaBean){
    		return ((DynaBean) o).getProperty(name);
    	}
    	
    	if(null == this.p){
    		synchronized (this) {
	            if(null == this.p){
	            	if(o instanceof Class<?>){
	            		this.c = (Class<?>)o;
	            		this.p = resolveProperty(context, c);
	            	}else{
	            		this.c = o.getClass();
	            		this.p = resolveProperty(context, o, c);
	            	}
	            }
    		}
    	}
    	
    	ElProperty p = this.p;
    	Class<?> c = cls(o);
    	if(this.c != c){
    		if(c == o){
    			p = resolveProperty(context, c);
    		}else{
    			p = resolveProperty(context, o, c);
    		}
    	}

    	try {
	        return p.getValue(context, o);
        } catch (Throwable e) {
        	throw new ElException(context.getMessage("el.errors.getPropertyError", name, o, e.getMessage()),e);
        }
    }
    
    protected ElProperty resolveProperty(ElEvalContext ctx, Class<?> c) {
    	ElProperty p = ctx.resolveProperty(c, name);
    	if(null == p){
    		throw new ElException(ctx.getMessage("el.errors.noSuchProperty",name,c));
    	}
    	return p;
    }
    
    protected ElProperty resolveProperty(ElEvalContext ctx, Object o, Class<?> c) {
    	ElProperty p = ctx.resolveProperty(o, c, name);
    	if(null == p){
    		throw new ElException(ctx.getMessage("el.errors.noSuchProperty",name,c));
    	}
    	return p;
    }
    
    static Class<?> cls(Object o){
    	Class<?> c = o.getClass();
    	if(c == Class.class){
    		return (Class<?>)o;
    	}else{
    		return c;
    	}
    }
    
	protected void doAccept(AstVisitor visitor) {
        if (visitor.startVisit(this)) {
        	acceptChild(visitor, this.owner);
        }

        visitor.endVisit(this);
    }
	
    public void toString(StringBuilder buf) {
        this.owner.toString(buf);
        buf.append(".");
        buf.append(this.name);
    }
}
