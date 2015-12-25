/**
 * Copyright (C) 2010 Mycila <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.junit.concurrent;

import java.util.concurrent.CountDownLatch;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ConcurrentRule implements TestRule {
	
	private static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 4;
	
	private int threads = DEFAULT_THREADS;
	
	private boolean runAnnotatedMethodOnly = false;
	
	public ConcurrentRule(){
		
	}
	
	public ConcurrentRule(int threads){
		this.threads = threads;
	}
	
	public ConcurrentRule(boolean runAnnotatedMethodOnly) {
		this.runAnnotatedMethodOnly = runAnnotatedMethodOnly;
	}
	
	public ConcurrentRule(int threads,boolean runAnnotatedMethodOnly) {
		this.threads                = threads;
		this.runAnnotatedMethodOnly = runAnnotatedMethodOnly;
	}
	
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
            	String testName = description.getTestClass().getSimpleName() + (description.isTest() ? "#" + description.getMethodName() : "");
            	
                Concurrent annotation = description.getAnnotation(Concurrent.class);
                
                if(null == annotation){
                	annotation = description.getTestClass().getAnnotation(Concurrent.class);
                }
                
            	if(description.getAnnotation(ConcurrentIgnore.class) != null){
            		base.evaluate();
            		return;
            	}
                
                if(runAnnotatedMethodOnly && null == annotation){
                	base.evaluate();
                	return;
                }
                
                //run concurrently
            	base.evaluate();
                
            	//System.out.println("wramup '" + testName + "' in single thread");
            	
                int threads = ConcurrentRule.this.threads;
                
                if(null != annotation ){
                	threads = Math.max(0, annotation.value());
                	
                	if(threads == 0){
                		threads = DEFAULT_THREADS;
                	}
                }
                
                if (threads> 1) {
                	int counter = threads;
                	
                    ConcurrentRunnerScheduler scheduler = new ConcurrentRunnerScheduler(testName, threads);
                    
                    final CountDownLatch go = new CountDownLatch(1);
                    
                    long start = System.currentTimeMillis();
                    
                    Runnable runnable = new Runnable() {
                        public void run() {
                            try {
                                go.await();
                                base.evaluate();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (Throwable throwable) {
                                throw ConcurrentRunnerException.wrap(throwable);
                            }
                        }
                    };
                    
                    while (counter-- > 0) {
                        scheduler.schedule(runnable);
                    }
                    
                    go.countDown();
                    
                    try {
                        scheduler.finished();
                    } catch (ConcurrentRunnerException e) {
                        throw e.unwrap();
                    }
                    
                    long end = System.currentTimeMillis();
                    
                    System.out.println("Run '" + testName + "' concurrently in " + threads + " threads,used " + (end-start) + "ms");
                }
            }
        };
    }
}
