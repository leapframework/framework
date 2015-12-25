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

public interface CriteriaWhere<T> {
	
	enum Op {
		EQ("="),
		LT("<"),
		LE("<="),
		GT(">"),
		GE(">="),
		IN("in"),
		LIKE("like"),
		NOT_IN("not in"),
		NOT_LIKE("not like");
		
		private final String value;
		
		private Op(String v) {
			this.value = v;
		}

		public String getValue() {
			return value;
		}
	}

	/**
	 * Returns the query for this where expression.
	 */
	CriteriaQuery<T> q();
	
	/**
	 * Adds and operator.
	 */
	default CriteriaWhere<T> and() {
		return append(" and ");
	}
	
	/**
	 * Adds an 'and' condition expression "and name op param", such as and("id","=",100);
	 */
	default CriteriaWhere<T> and(String name,String op,Object param) {
		return and().cnd(name, op, param);
	}
	
	/**
	 * Adds an 'and' condition expression "and name op param", such as and("id",100);
	 */
	default CriteriaWhere<T> and(String name,Object param) {
		return and().cnd(name, param);
	}
	
	/**
	 * Adds or operator.
	 */
	default CriteriaWhere<T> or() {
		return append(" or ");
	}
	
	/**
	 * Adds an 'or' condition expression "and name op param", such as and("id","=",100);
	 */
	default CriteriaWhere<T> or(String name,String op,Object param) {
		return or().cnd(name, op, param);
	}
	
	/**
	 * Adds an 'or' condition expression "and name op param", such as and("id",,100);
	 */
	default CriteriaWhere<T> or(String name,Object param) {
		return or().cnd(name, param);
	}
	
	/**
	 * Adds a condition expression "name op param", such as cnd("id","=",100);
	 */
	default CriteriaWhere<T> cnd(String name,String op,Object param) {
		return name(name).op(op).param(param);
	}
	
	/**
	 * Adds a condition expression "name op param", such as cnd("id","=",100);
	 */
	default CriteriaWhere<T> cnd(String name,Op op,Object param) {
		return name(name).op(op.getValue()).param(param);
	}
	
	/**
	 * Adds a condition expression "name = param", such as cnd("id",100);
	 */
	default CriteriaWhere<T> cnd(String name,Object param) {
		return name(name).op(Op.EQ.getValue()).param(param);
	}
	
	/**
	 * Adds a name.
	 */
	CriteriaWhere<T> name(String name);
	
	/**
	 * Adds a operator.
	 */
	CriteriaWhere<T> op(String op);
	
	/**
	 * Adds a parameter.
	 */
	CriteriaWhere<T> param(Object p);
	
	/**
	 * Adds a in operator with the given parameter.
	 */
	default CriteriaWhere<T> in(Object... array) {
		return op(Op.IN.getValue()).param(array);
	}
	
	/**
	 * Adds a not in operator with the given parameter.
	 */
	default CriteriaWhere<T> not_in(Object... array) {
		return op(Op.NOT_IN.getValue()).param(array);
	}
	
	/**
	 * Adds a like operator with the given parameter.
	 */
	default CriteriaWhere<T> like(String pattern) {
		return op(Op.LIKE.getValue()).param(pattern);
	}
	
	/**
	 * Adds a not like operator with the given parameter.
	 */
	default CriteriaWhere<T> not_like(String pattern) {
		return op(Op.NOT_LIKE.getValue()).param(pattern);
	}
	
	/**
	 * Adds a like operator with the parameter <code>string%</code>.
	 */
	default CriteriaWhere<T> starts_with(String string) {
		return like(null == string ? "%" : string + "%");
	}
	
	/**
	 * Adds a like operator with the parameter <code>%string</code>.
	 */
	default CriteriaWhere<T> ends_with(String string) {
		return like(null == string ? "%" : "%" + string);
	}
	
	/**
	 * Adds a like operator with the parameter <code>%string%</code>.
	 */
	default CriteriaWhere<T> contains(String string) {
		return like((null == string ? "%" : "%" + string + "%"));
	}
	
	/**
	 * Adds a '=' operator with the given parameter.
	 */
	default CriteriaWhere<T> eq(Object param) {
		return op(Op.EQ.getValue()).param(param);
	}
	
	/**
	 * Adds a '<' operator with the given parameter.
	 */
	default CriteriaWhere<T> lt(Object param) {
		return op(Op.LT.getValue()).param(param);
	}
	
	/**
	 * Adds a '<=' operator with the given parameter.
	 */
	default CriteriaWhere<T> le(Object param) {
		return op(Op.LE.getValue()).param(param);
	}
	
	/**
	 * Adds a '>' operator with the given parameter.
	 */
	default CriteriaWhere<T> gt(Object param) {
		return op(Op.GT.getValue()).param(param);
	}
	
	/**
	 * Adds a '>=' operator with the given parameter.
	 */
	default CriteriaWhere<T> ge(Object param) {
		return op(Op.GE.getValue()).param(param);
	}
	
	/**
	 * Appends a string expression.
	 */
	CriteriaWhere<T> append(String expr);
}