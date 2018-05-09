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
package leap.orm.sql.ast;

import leap.core.el.ExpressionLanguage;
import leap.lang.Strings;
import leap.lang.Try;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.*;
import leap.orm.sql.parser.Lexer;
import leap.orm.sql.parser.SqlParser;

import java.io.IOException;
import java.util.Map;

public class Tag extends DynamicNode implements SqlTag {

    private static final Log log = LogFactory.get(Tag.class);

    protected final String  name;
    protected final String  content;

    protected Map<String, Object> vars;
    protected Object              executionObject;
    protected ExpressionLanguage  el;
    protected SqlTagProcessor     processor;

    public Tag(String name, String content) {
        this.name = name;
        this.content = content.trim();
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    @Override
    public void setVars(Map<String, Object> vars) {
        this.vars = vars;
    }

    @Override
    public Object getExecutionObject() {
        return executionObject;
    }

    @Override
    public void setExecutionObject(Object o) {
        this.executionObject = o;
    }

    @Override
    protected void toString_(Appendable buf) throws IOException {
        buf.append("@").append(name).append("(");

        buf.append(content).append(")");
    }

    @Override
    public void prepare(MetadataContext context, Sql sql) {
        processor = context.getAppContext().getBeanFactory().tryGetBean(SqlTagProcessor.class, name);
        if(null == processor) {
            throw new SqlConfigException("Sql tag processor '" + name + "' not exists, check it : " + toString());
        }else{
            processor.prepareTag(context, sql, this);
        }

        el = context.getAppContext().getBeanFactory().getBean(ExpressionLanguage.class);
    }

	@Override
    protected final void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
        if(null != processor) {
            String expr = Strings.trim(processor.processTag(context, sql, this, params));

            if(!Strings.isEmpty(expr)) {
                buildStatement(context, sql, stm, params, expr);
            }
        }
    }

    @Override
    public void resolveDynamic(SqlContext context, Sql sql, Appendable buf, Params params) {
        if(null != processor) {
            String s = processor.toFragment(context, sql, this, params);
            if(null != s) {
                Try.catchAll(() -> buf.append(s));
                return;
            }
        }
        super.resolveDynamic(context, sql, buf, params);
    }

    public String process(SqlContext context, Sql sql, Params params) {
        if(null == processor) {
            return Strings.EMPTY;
        }else{
            return processor.processTag(context, sql, this, params);
        }
    }

    public void buildStatement(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params, String text) throws IOException {

        if(log.isDebugEnabled()) {
            log.debug("Tag {} -> {}", toString(), text);
        }


        SqlParser parser = new SqlParser(new Lexer(text, Sql.ParseLevel.MORE), el);
        SqlWhereExpr expr = parser.whereExpr();

        expr.buildStatement(context, sql, stm, params);
    }
}
