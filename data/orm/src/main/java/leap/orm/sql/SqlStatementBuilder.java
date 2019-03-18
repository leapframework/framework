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
package leap.orm.sql;

import leap.db.Db;
import leap.db.DbDialect;
import leap.lang.Buildable;

import java.util.List;
import java.util.Map;

public interface SqlStatementBuilder extends Appendable,Buildable<SqlStatement> {

    /**
     * Returns the {@link DbDialect}.
     */
    default DbDialect dialect() {
        return context().getOrmContext().getDb().getDialect();
    }

    /**
     * Returns the {@link Db}.
     */
    default Db db() {
        return context().getOrmContext().getDb();
    }
	
	/**
	 * Returns the {@link SqlContext}.
	 */
	SqlContext context();
	
	/**
	 * Returns <code>true</code> if the target {@link SqlStatement} is a query.
	 */
	boolean isQuery();

    /**
     * Returns the vars for eval expression.
     */
    Map<String, Object> getVars();

    /**
     * Sets the vars for eval expression.
     */
    void setVars(Map<String, Object> vars);

    /**
     * Returns the sql text.
     */
    StringBuilder getText();

    /**
     * Returns the sql args.
     */
    List<Object> getArgs();

	/**
	 * Starts from 0
	 */
	int increaseAndGetParameterIndex();
	
	/**
	 * Returns current parameter index.
	 */
	int currentParameterIndex();
	
	/**
	 * Adds a parameter value to this builder. 
	 * 
	 * The index returned by {@link #increaseAndGetParameterIndex()} will be increased.
	 */
	SqlStatementBuilder addParameter(Object value);
	
	/**
	 * todo : doc
	 */
	boolean isLastInOperator();
	
	/**
	 * todo : doc
	 */
	int removeLastEqualsOperator();

    /**
     * Creates a new {@link SavePoint}.
     */
    SavePoint createSavePoint();

    /**
     * Records a state of statement.
     */
    interface SavePoint {

        /**
         * Reset the state of statement builder to this save point.
         */
        void restore();

        /**
         * Returns true if the statement builder has changes from this save point.
         */
        boolean hasChanges();

        /**
         * Removes the appended text from the save point and return it.
         */
        String removeAppendedText();
    }
}