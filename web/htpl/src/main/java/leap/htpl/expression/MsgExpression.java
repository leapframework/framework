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
package leap.htpl.expression;

import java.util.Map;

import leap.htpl.HtplContext;
import leap.lang.expression.AbstractExpression;

public class MsgExpression extends AbstractExpression {

	public static final String PREFIX = "#{";
	public static final String SUFFIX = "}";
	
	private String key;
	
	public MsgExpression(String key) {
		this.key = key;
	}
	
	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
		HtplContext tc = (HtplContext)context;
		String s = tc.getMessageSource().tryGetMessage(tc.getLocale(), key);
		return null == s ? key : s;
    }

}