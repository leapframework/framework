/*
 * Copyright 2015 the original author or authors.
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
package leap.db.platform.ansi;

import leap.db.platform.GenericDbDialect;

public class AnsiDbDialect extends GenericDbDialect {

	public AnsiDbDialect() {
		
	}

	@Override
	protected String getTestDriverSupportsGetParameterTypeSQL() {
		//TODO: 
		return "select 1 from dual";
	}

	@Override
	protected String getOpenQuoteString() {
		//TODO:
		return "\"";
	}

	@Override
	protected String getCloseQuoteString() {
		//TODO:
		return "\"";
	}

	@Override
	protected void registerColumnTypes() {
		//TODO:
	}

}