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
package app.controllers;

import leap.lang.http.HTTP;
import leap.web.exception.ResponseException;

public class ErrorTestController {

	public void err404() {
		throw new ResponseException(HTTP.SC_NOT_FOUND);
	}
	
	public void err403() {
		throw new ResponseException(HTTP.SC_FORBIDDEN);
	}

	public void err500() {
		throw new RuntimeException("err");
	}
	
	public void errCustom(){
		throw new CustomeException("err");
	}
	
	public static final class CustomeException extends RuntimeException {

		private static final long serialVersionUID = -6199374679613539141L;

		public CustomeException() {
	        super();
        }

		public CustomeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	        super(message, cause, enableSuppression, writableStackTrace);
        }

		public CustomeException(String message, Throwable cause) {
	        super(message, cause);
        }

		public CustomeException(String message) {
	        super(message);
        }

		public CustomeException(Throwable cause) {
	        super(cause);
        }
		
	}
}
