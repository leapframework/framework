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

public class AstChoice extends AstExpr {
	private AstExpr question;
	private AstExpr yes;
	private AstExpr no;

	public AstChoice() {

	}

	public AstChoice(AstExpr question, AstExpr yes, AstExpr no) {
		this.question = question;
		this.yes = yes;
		this.no = no;
	}

	public AstExpr getQuestion() {
		return question;
	}

	public void setQuestion(AstExpr question) {
		this.question = question;
	}

	public AstExpr getYes() {
		return yes;
	}

	public void setYes(AstExpr yes) {
		this.yes = yes;
	}

	public AstExpr getNo() {
		return no;
	}

	public void setNo(AstExpr no) {
		this.no = no;
	}
	
	@Override
    public Object eval(ElEvalContext context) {
	    return context.test(question.eval(context)) ? yes.eval(context) : no.eval(context);
    }

	@Override
	protected void doAccept(AstVisitor visitor) {
		if (visitor.startVisit(this)) {
			acceptChild(visitor, question);
			acceptChild(visitor, yes);
			acceptChild(visitor, no);
		}
		visitor.endVisit(this);
	}
}
