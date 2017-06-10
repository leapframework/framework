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

import leap.lang.Strings;
import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.*;

import java.io.IOException;

public class Tag extends DynamicNode implements SqlTag {

    protected final String  name;
    protected final String  content;
    protected final boolean optional;

    protected Object          executionObject;
    protected SqlTagProcessor processor;

    public Tag(String name, String content, boolean optional) {
        this.name = name;
        this.content = content.trim();
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public boolean isOptional() {
        return optional;
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
        buf.append("@").append(name).append("{");

        if(optional){
            buf.append('?');
        }

        buf.append(content).append("}");
    }

    @Override
    public void prepare(MetadataContext context) {
        processor = context.getAppContext().getBeanFactory().tryGetBean(SqlTagProcessor.class, name);
        if(null == processor) {
            if(!optional) {
                throw new SqlConfigException("Tag processor '" + name + "' not exists, check it : " + toString());
            }
        }else{
            processor.prepareTag(context, this);
        }
    }

	@Override
    protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
        if(null != processor) {
            String expr = processor.processTag(context, this, params);
            if(!Strings.isEmpty(expr)) {
                buildStatement(context, stm, params, expr);
            }
        }
    }

    public String process(SqlContext context, Params params) {
        if(null == processor) {
            return Strings.EMPTY;
        }else{
            return processor.processTag(context, this, params);
        }
    }

    public void buildStatement(SqlContext context, SqlStatementBuilder stm, Params params, String expr) throws IOException {
        //todo :
        stm.append(expr);
    }
}
