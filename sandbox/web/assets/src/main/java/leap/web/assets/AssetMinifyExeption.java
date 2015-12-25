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
package leap.web.assets;

public class AssetMinifyExeption extends AssetException {

	private static final long serialVersionUID = -6229663393756014116L;

	public AssetMinifyExeption() {
	}

	public AssetMinifyExeption(String message) {
		super(message);
	}

	public AssetMinifyExeption(Throwable cause) {
		super(cause);
	}

	public AssetMinifyExeption(String message, Throwable cause) {
		super(message, cause);
	}

}