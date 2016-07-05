/*
 * Copyright 2012 the original author or authors.
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
package leap.junit.contexual;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ContextualRule implements TestRule {
	private final boolean			 runAnnotatedOnly;
	private final ContextualProvider provider;

	public ContextualRule(ContextualProvider provider) {
	    super();
	    this.provider         = provider;
	    this.runAnnotatedOnly = false;
    }
	
	public ContextualRule(ContextualProvider provider,boolean runAnnotatedOnly) {
	    super();
	    this.provider         = provider;
	    this.runAnnotatedOnly = runAnnotatedOnly;
    }

	public Statement apply(final Statement base,final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				
		    	if(description.getAnnotation(ContextualIgnore.class) != null){
		    		base.evaluate();
		    		return ;
		    	}
				
				boolean isDefaultContextual;
				
				if(description.getTestClass().isAnnotationPresent(Contextual.class)){
					isDefaultContextual = true;
				}else if(description.getTestClass().isAnnotationPresent(ContextualIgnore.class)){
					isDefaultContextual = false;
				}else {
					isDefaultContextual = !runAnnotatedOnly;
				}
		    	
		    	Contextual contextual = description.getAnnotation(Contextual.class);
		    	
		    	if(!isDefaultContextual && contextual == null){
		    		base.evaluate();
		    		return;
		    	}
		    	
		    	if(null != contextual){
		    		String   contextName  = contextual.value();
		    		String[] contextNames = contextual.values();
		    		
		    		if(null != contextName && contextName.length() > 0){
		    			contextNames = new String[]{contextName};
		    		}
		    		
		    		//boolean executed = false;
		    		
		    		for(String name : provider.names(description)){
		    			if(isExecute(name,contextNames)){
				    		try{
				    			System.out.println("\n------------------------------------------------------------------------------------------------------\n" + 
				    								"              running test case '" + description.getMethodName() + "' for context '" + name + "'" + 
				    							   "\n------------------------------------------------------------------------------------------------------\n");
				    			provider.beforeTest(description, name);
				    			base.evaluate();
				    			//executed = true;
				    		}finally{
				    			provider.afterTest(description,name);
				    		}
		    			}else{
                            System.out.println("Test case '" + description.getMethodName() + "' not executed because the context name '" + name + "' not exists!");
                        }
		    		}

                    /*
		    		if(!executed){
		    			throw new IllegalArgumentException("Unknown contextual qualifier : " + contextName);
		    		}
		    		*/
		    	}else{
		    		boolean hasNames = false;
		    		
			    	for(String name : provider.names(description)){
			    		hasNames = true;
			    		try{
			    			provider.beforeTest(description, name);
			    			base.evaluate();
			    		}finally{
			    			provider.afterTest(description,name);
			    		}
			    	}
			    	
			    	if(!hasNames){
			    		base.evaluate();
			    	}
		    	}
		    	
		    	provider.finishTests(description);
			}
		};
    }
	
	private boolean isExecute(String name,String[] contextNames){
		if(contextNames.length == 0){
			return true;
		}
		
		for(String contextName : contextNames){
			if(name.equalsIgnoreCase(contextName)){
				return true;
			}
		}
		return false;
	}
}
