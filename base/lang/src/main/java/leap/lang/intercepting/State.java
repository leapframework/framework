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
package leap.lang.intercepting;

public class State {

	public static final int SC_CONTINUE           = -1;
	public static final int SC_CONTINUE_PROCESSED = 0;
	public static final int SC_INTERCEPTED        = 1;
	
	public static State CONTINUE           = new State(SC_CONTINUE);
	public static State CONTINUE_PROCESSED = new State(SC_CONTINUE_PROCESSED);
	public static State INTERCEPTED        = new State(SC_INTERCEPTED);
	
	public static boolean isContinue(State s) {
		return null == s || s.isContinue();
	}
	
	public static boolean isProcessed(State s) {
	    return null != s && s.isProcessed();
	}
	
	public static boolean isIntercepted(State s) {
		return null != s && s.isIntercepted();
	}
	
	protected final int status;
	
	public boolean isContinue() {
		return status == SC_CONTINUE || status == SC_CONTINUE_PROCESSED;
	}
	
	public boolean isProcessed() {
	    return status == SC_CONTINUE_PROCESSED;
	}
	
	public boolean isIntercepted() {
		return status == SC_INTERCEPTED;
	}
	
	public int getStatus() {
		return status;
	}

	protected State(int status) {
		this.status = status;
	}
	
}
