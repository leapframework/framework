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
package leap.core;

public class AppInitException extends AppException {

	private static final long serialVersionUID = 2758343942189747493L;

	public AppInitException() {
	}

	public AppInitException(String message) {
		super(message);
	}

	public AppInitException(Throwable cause) {
		super(cause);
	}

	public AppInitException(String message, Throwable cause) {
		super(message, cause);
	}

}