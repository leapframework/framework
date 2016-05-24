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
package leap.lang.el;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import leap.lang.Arrays2;
import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectField;
import leap.lang.reflect.ReflectMethod;

public abstract class AbstractElEvalContext extends AbstractElContext implements ElEvalContext {
	
	protected Object				  ctx;
	protected Map<String, Object> 	  vars;
	protected Map<String, ElFunction> functions;
	
	private Map<Object, Enumerable<?>> arrays;
	
	public AbstractElEvalContext() {
		this(null);
	}
	
	public AbstractElEvalContext(Object ctx) {
		this.ctx  = ctx;
		this.vars = new HashMap<>();
	}

    public AbstractElEvalContext(Object ctx,Map<String, Object> vars) {
    	this.ctx  = ctx;
    	this.vars = null == vars ? new HashMap<String,Object>() : vars;
    }
    
	@Override
    public Object getRoot() {
	    return ctx;
    }

	@Override
	public Object resolveVariable(String name) {
		Object v = vars.get(name);
		
		if(null == v && null != ctx && !vars.containsKey(name)){
			v = resolveContextProperty(name);
		}
		
		return v;
	}
	
	@Override
    public boolean isVariableResolved(String name) {
	    return vars.containsKey(name);
    }
	
	@Override
    public ElMethod resolveMethod(Object owner, Class<?> cls, String name, Object[] args) {
	    return resolveMethod(cls, name, args);
    }
	
	@Override
    public ElMethod resolveMethod(Class<?> cls, String name, Object[] args) {
		ReflectClass rc = ReflectClass.of(cls);

		int len = args.length;
		
		for(ReflectMethod rm : rc.getMethods()){
			if(rm.getName().equals(name) && rm.getParameters().length == len){
				boolean ok = true;
				for(int i=0;i<len;i++){
					Object v = args[i];
					if(null != v && !rm.getParameters()[i].getType().isAssignableFrom(v.getClass())){
						ok = false;
						break;
					}
				}
				if(ok){
					return rm.isStatic() ? new ElStaticMethod(rm) : new ElInstanceMethod(rm);	
				}
			}
		}
		
	    return null;
    }
	
	@Override
    public ElProperty resolveProperty(Object owner, Class<?> cls, String name) {
		BeanType bt = BeanType.of(cls);
		BeanProperty bp = bt.tryGetProperty(name);
		if(null != bp){
			return new ElBeanProperty(bp);
		}
		return resolveProperty(cls, name);
    }

	@Override
    public ElProperty resolveProperty(Class<?> cls, String name) {
		ReflectClass rc = ReflectClass.of(cls);
		ReflectField rf = rc.getField(name);
		if(null != rf && rf.isPublicGet()){
			if(rf.isStatic()){
				return rf.isFinal() ? ElConstantField.of(rf) : new ElStaticField(rf);
			}else{
				return new ElInstanceField(rf);	
			}
		}
		
		final ElMethod m = resolveMethod(cls, name, Arrays2.EMPTY_OBJECT_ARRAY);
		if(null != m) {
			return new ElProperty() {
				@Override
				public Object getValue(ElEvalContext context, Object instance) throws Throwable {
					return m.invoke(context, instance, Arrays2.EMPTY_OBJECT_ARRAY);
				}
			};
		}
		
		return null;
    }

	@Override
    public ElFunction resolveFunction(String fullName) {
	    return null == functions ? null : functions.get(fullName);
    }

	@Override
    public Object getArrayItem(Object a, int index) {
	    return array(a).get(index);
    }
	
	public Map<String, ElFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(Map<String, ElFunction> functions) {
		this.functions = functions;
	}
	
	@SuppressWarnings("rawtypes")
    protected Object resolveContextProperty(String name){
		if(ctx instanceof Map){
			Map map = (Map)ctx;
			if(map.containsKey(name)){
				Object v = map.get(name);
				vars.put(name, v);
				return v;
			}
		}else{
			ElProperty p = resolveProperty(ctx.getClass(), name);
			if(null != p){
				try {
	                Object v = p.getValue(this,ctx);
	                vars.put(name, v);
	                return v;
	            } catch (Throwable e) {
	            	throw new ElException("Error get value from property '" + name + "' in class '" + ctx.getClass() + "', " + e.getMessage(), e);
	            }
			}
		}
		return null;
	}

	private Enumerable<?> array(Object a) {
		if(null == arrays){
			arrays = new IdentityHashMap<>();
			
			Enumerable<?> e = Enumerables.of(a);
			arrays.put(a, e);
			
			return e;
		}else{
			Enumerable<?> e = arrays.get(a);
			if(null == e){
				e = Enumerables.of(a);
				arrays.put(a, e);
			}
			return e;
		}
	}
}