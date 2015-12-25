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
package leap.web.exception;

import leap.core.AppException;
import leap.web.Content;

public class ResponseException extends AppException {

	private static final long serialVersionUID = -2431558403363115801L;
	
	private final int 	  status;
	private final Content content;

	public ResponseException(int status) {
		this.status  = status;
		this.content = null;
	}

	public ResponseException(int status, String message) {
		super(message);
		this.status  = status;
		this.content = null;
	}

	public ResponseException(int status, Throwable cause) {
		super(cause);
		this.status  = status;
		this.content = null;
	}
	
	public ResponseException(int status, Content content) {
		this.status  = status;
		this.content = content;
	}

	public ResponseException(int status, String message, Throwable cause) {
		super(message, cause);
		this.status  = status;
		this.content = null;
	}
	
	public ResponseException(int status, String message, Content content) {
		super(message);
		this.status  = status;
		this.content = content;
	}
	
	public ResponseException(int status, String message, Content content, Throwable cause) {
		super(message, cause);
		this.status  = status;
		this.content = content;
	}

	public int getStatus() {
		return status;
	}
	
	public Content getContent() {
		return content;
	}
}