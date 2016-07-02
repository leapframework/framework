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
package leap.lang.el.spel.ast;

import leap.lang.el.ElEvalContext;

public class AstNull extends AstLiteral {
	
    public AstNull() {
	    super();
    }

	@Override
    public Object eval(ElEvalContext context) {
	    return null;
    }

	protected void doAccept(AstVisitor visitor) {
        visitor.startVisit(this);
        visitor.endVisit(this);
    }
    
    public void toString(StringBuilder buf) {
        buf.append("null");
    }

	@Override
    public Object getLiteralValue() {
	    return null;
    }
}