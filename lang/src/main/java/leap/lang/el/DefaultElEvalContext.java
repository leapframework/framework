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
package leap.lang.el;

import java.util.Map;

public class DefaultElEvalContext extends AbstractElEvalContext implements ElEvalContext {
	
	public DefaultElEvalContext() {
	    super();
    }
	
	public DefaultElEvalContext(Object ctx) {
	    super(ctx);
    }

	public DefaultElEvalContext(Map<String, Object> vars) {
	    super(vars);
    }

	public DefaultElEvalContext(Object ctx, Map<String, Object> vars) {
	    super(ctx, vars);
    }
	
	@Override
    public String getMessage(String key, Object... args) {
		return ElMessages.get(key, args);
	}

}