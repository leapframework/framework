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

import java.io.PrintWriter;
import java.io.StringWriter;


public abstract class AstExpr extends AstNode {

    private AstExpr parent;

    public AstExpr getParent() {
        return parent;
    }

    public void setParent(AstExpr parentExpr) {
        this.parent = parentExpr;
    }

    @Override
    public void toString(StringBuilder buf) {
        StringWriter out = new StringWriter();
        PrintVisitor visitor = new PrintVisitor(new PrintWriter(out));
        this.accept(visitor);
        buf.append(out.toString());
    }
    
    public boolean isBoolean() {
    	if(null == resultType) {
    		return false;
    	}
    	return Boolean.TYPE == resultType || Boolean.class == resultType;
    }

}