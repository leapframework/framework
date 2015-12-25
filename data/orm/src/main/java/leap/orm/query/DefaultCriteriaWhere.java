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
package leap.orm.query;

import leap.lang.Args;
import leap.lang.Assert;
import leap.lang.params.MapParams;
import leap.lang.params.Params;

public class DefaultCriteriaWhere<T> implements CriteriaWhere<T> {

	private final CriteriaQuery<T> q;
	private final StringBuilder    expr   = new StringBuilder();
	private final Params       	   params = new MapParams();
	
	private String name;
	private String op;

	public DefaultCriteriaWhere(CriteriaQuery<T> q) {
		this.q = q;
	}

	@Override
    public CriteriaQuery<T> q() {
	    q.where(expr.toString()).params(params);
	    return q;
    }

	@Override
    public CriteriaWhere<T> name(String name) {
		Args.notEmpty(name,"name");
		Assert.isNull(this.name,"Value must be specified for previous parameter '" + name + "'");
		append(name);
		this.name = name;
	    return this;
    }
	
	@Override
    public CriteriaWhere<T> op(String op) {
		Args.notEmpty(op,"operator");
		Assert.isNull(this.op,"Previous operator '" + this.op + "' must be clear by calling param(p)");
		this.op = op;
		expr.append(' ').append(op).append(' ');
	    return this;
    }

	@Override
    public CriteriaWhere<T> param(Object p) {
		return param(null,p);
	}
	
    public CriteriaWhere<T> param(String name, Object p) {
		Assert.notNull(this.name,"Parameter name must be specified before setting parameter value");
		String pname = null == name ? this.name : name;
		append(":" + pname);
		params.set(pname, p);
		this.name = null;
		this.op   = null;
		return this;
    }

	@Override
    public CriteriaWhere<T> append(String expr) {
		this.expr.append(expr);
	    return this;
    }
}