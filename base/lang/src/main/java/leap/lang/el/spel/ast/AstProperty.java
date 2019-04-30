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

import leap.lang.Named;
import leap.lang.accessor.Getter;
import leap.lang.accessor.ObjectPropertyGetter;
import leap.lang.el.*;

import java.util.Map;


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
    		return ((ElPropertyResolver) o).resolveProperty(name, context);
    	}

        boolean dyna;
    	Object v = null;
    	if(o instanceof ObjectPropertyGetter){
            dyna = true;
    		v = ((ObjectPropertyGetter) o).getProperty(name);
    	}else if(o instanceof Getter) {
            dyna = true;
            v = ((Getter) o).get(name);
        }else {
    	    dyna = false;
        }
        if(null != v) {
            return v;
        }

        if(ElProperty.NULL == this.p) {
            return null;
        }
    	
    	if(null == this.p){
    		synchronized (this) {
	            if(null == this.p){
	            	if(o instanceof Class<?>){
	            		this.c = (Class<?>)o;
	            		this.p = resolveProperty(context, c, false);
	            	}else{
	            		this.c = o.getClass();
	            		this.p = resolveProperty(context, o, c, dyna);
	            	}
	            }
    		}
    	}
    	
    	ElProperty p = this.p;
    	Class<?> c = cls(o);
    	if(this.c != c){
    		if(c == o){
    			p = resolveProperty(context, c, dyna);
    		}else{
    			p = resolveProperty(context, o, c, dyna);
    		}
    	}

    	if(ElProperty.NULL == p) {
    	    return null;
        }

    	try {
	        return p.getValue(context, o);
        } catch (Throwable e) {
        	throw new ElException(context.getMessage("el.errors.getPropertyError", name, o, e.getMessage()),e);
        }
    }
    
    protected ElProperty resolveProperty(ElEvalContext ctx, Class<?> c, boolean dyna) {
    	ElProperty p = ctx.resolveProperty(c, name);
    	if(null == p){
            if(dyna) {
                return ElProperty.NULL;
            }
    		throw new ElException(ctx.getMessage("el.errors.noSuchProperty",name,c));
    	}
    	return p;
    }
    
    protected ElProperty resolveProperty(ElEvalContext ctx, Object o, Class<?> c, boolean dyna) {
    	ElProperty p = ctx.resolveProperty(o, c, name);
    	if(null == p){
    	    if(dyna) {
    	        return ElProperty.NULL;
            }
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
