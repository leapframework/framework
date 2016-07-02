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
package leap.lang;

public class Confirm {
	
	private static final ThreadLocal<Boolean> confirm = new ThreadLocal<Boolean>();
	
	/**
	 * Example : 
	 * 
	 * <pre>
	 * Confirms.execute(new Runnable(){
	 * 
	 *     public void run() {
	 *         //perform the opertions which will ask for confirm.
	 *     }
	 * 
	 * })
	 * </pre>
	 */
	public static void execute(Runnable runnable) {
		try{
			confirm.set(Boolean.TRUE);
			runnable.run();
		}finally{
			confirm.remove();
		}
	}
	
	public static boolean isConfirmed() {
		Boolean value = confirm.get();
		return null != value;
	}
	
	public static void checkConfirmed(String action) throws ConfirmException {
		if(!isConfirmed()){
			throw new ConfirmException("Action '" + action + "' must be confirmed, please use method '" + 
									   Confirm.class.getName() + ".execute(Action)' to execute it.");
		}
	}
	
	public static void checkConfirmed(String action,String reason) throws ConfirmException {
		if(!isConfirmed()){
			throw new ConfirmException("Action '" + action + "' must be confirmed for the reason '" + reason + "', please use method '" + 
									   Confirm.class.getName() + ".execute(Action)' to execute it.");
		}
	}
	
	public static class ConfirmException extends RuntimeException {

		private static final long serialVersionUID = -1820589737343972497L;

		public ConfirmException(String message) {
	        super(message);
        }
	}
	
	protected Confirm(){
		
	}
	
}
