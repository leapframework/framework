/*
 * Copyright 2013 the original author or authors.
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
package leap.web.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import leap.core.annotation.Transactional;
import leap.core.transaction.DefaultTransactionDefinition;
import leap.core.transaction.TransactionDefinition;
import leap.core.transaction.TransactionDefinition.PropagationBehaviour;
import leap.lang.Args;
import leap.lang.New;
import leap.lang.reflect.ReflectException;
import leap.lang.reflect.ReflectMethod;

public class MethodAction implements Action {
	
	private static final ActionInterceptor[] EMPTY_INTERCEPTORS = new ActionInterceptor[]{};
	
	private final Object				 controller;
	private final ReflectMethod 		 method;
	private final Argument[]			 arguments;
	private final boolean				 hasReturnValue;
	private final boolean				 hasArguments;
	private final TransactionDefinition  transactionDefinition;
	private final ActionInterceptor[]    interceptors;
	private final Annotation[]			 mergedAnnotations;
	
	public MethodAction(Object controller, 
						ReflectMethod method, 
						Argument[] arguments,
						ActionInterceptor[] interceptors){
		Args.notNull(method,"method");
		Args.notNull(arguments,"arguments");
		
		this.controller            = controller;
		this.method                = method;
		this.arguments             = arguments;
		this.hasReturnValue        = method.hasReturnValue();
		this.hasArguments          = arguments.length > 0;
		this.interceptors	       = null == interceptors ? EMPTY_INTERCEPTORS : interceptors;
		this.transactionDefinition = resolveTransactionDefinition();
		this.mergedAnnotations     = mergeAnnotations();
		
		/*
		Cors cors = this.searchAnnotation(Cors.class);
		this.corsEnabled  = null != cors && cors.value()  ? true : false;
		this.corsDisabled = null != cors && !cors.value() ? true : false;
		*/
	}
	
	@Override
    public String getName() {
	    return method.getName();
    }
	
	@Override
    public boolean hasReturnValue() {
	    return hasReturnValue;
    }
	
	@Override
    public boolean hasArguments() {
	    return hasArguments;
    }

	@Override
    public Class<?> getReturnType() {
	    return method.getReturnType();
    }

	@Override
    public Type getGenericReturnType() {
	    return method.getReflectedMethod().getGenericReturnType();
    }

	@Override
    public Annotation[] getControllerAnnotations() {
	    return method.getReflectiveClass().getAnnotations();
    }
	
    public Annotation[] getAnnotations() {
	    return method.getAnnotations();
    }
    
	@Override
    public Annotation[] getMergedAnnotations() {
	    return mergedAnnotations;
    }

	@Override
    public Argument[] getArguments() {
	    return arguments;
    }
	
	@Override
    public ActionInterceptor[] getInterceptors() {
	    return interceptors;
    }

	@Override
    public Object execute(ActionContext context, Object[] args) {
		try {
	        return method.isStatic() ? method.invokeStatic(args) : method.invoke(controller, args);
        } catch (Exception e) {
        	if(e instanceof ReflectException && null != e.getCause()){
        		handleExecuteError(context, e.getCause());
        	}else{
        		handleExecuteError(context, e);	
        	}
        }
		return null;
    }
	
	@Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T searchAnnotation(Class<T> at) {
		for(Annotation a : getAnnotations()){
			if(a.annotationType().equals(at)){
				return (T)a;
			}
			if(a.annotationType().isAnnotationPresent(at)){
				return (T)a.annotationType().getAnnotation(at);
			}
		}
		for(Annotation a : getControllerAnnotations()){
			if(a.annotationType().equals(at)){
				return (T)a;
			}
			if(a.annotationType().isAnnotationPresent(at)){
				return (T)a.annotationType().getAnnotation(at);
			}
		}
		return null;
    }
	
    public boolean isTransactional() {
		return null != transactionDefinition;
	}

    public TransactionDefinition getTransactionDefinition() {
		return transactionDefinition;
	}
	
	@Override
    public String toString() {
		return method.getReflectiveClass().getReflectedClass().getSimpleName() + "." + method.getName();
    }
	
	protected Annotation[] mergeAnnotations() {
		List<Annotation> list = New.arrayList(getControllerAnnotations());
		
		for(Annotation aa : getAnnotations()) {
			int index = -1;
			
			for(int i=0;i<list.size();i++) {
				Annotation ca = list.get(i);
				
				if(ca.annotationType().equals(aa.annotationType())) {
					index = i;
					break;
				}
			}
			
			if(index >= 0) {
				list.set(index, aa);
			}else{
				list.add(aa);
			}
		}
		
		return list.toArray(new Annotation[list.size()]);
	}
	
	protected TransactionDefinition resolveTransactionDefinition() {
		Transactional conf = searchAnnotation(Transactional.class);
		
		if(null != conf && conf.value()) {
			DefaultTransactionDefinition td = new DefaultTransactionDefinition();
			
			if(conf.requiresNew()) {
				td.setPropagationBehavior(PropagationBehaviour.REQUIRES_NEW);
			}else{
				td.setPropagationBehavior(conf.propagationBehaviour());	
			}
			
			return td;
		}
		
		return null;
	}
	
	protected void handleExecuteError(ActionContext context, Throwable e){
		throwException(context, e);
		/*
		if(null != exceptionHandler) {
			try {
	            exceptionHandler.onActionException(context, e);
            } catch (Throwable e1) {
            	throwException(context, e1);
            }
		}else{
			throwException(context, e);
		}
		*/
	}
	
	protected void throwException(ActionContext context, Throwable e) {
		if(e instanceof RuntimeException){
			throw (RuntimeException)e;
		}
		throw new ActionException("Error executing action '" + context.getPath() + "' : " + e.getMessage(),e);
	}
}