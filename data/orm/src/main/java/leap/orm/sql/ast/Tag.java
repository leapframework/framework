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

import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.*;

import java.io.IOException;

public class Tag extends DynamicNode implements SqlTag {

    protected final String name;
    protected final String content;

    protected Object          executionObject;
    protected SqlTagProcessor processor;

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
    public Object getExecutionObject() {
        return executionObject;
    }

    @Override
    public void setExecutionObject(Object o) {
        this.executionObject = o;
    }

    @Override
    public void prepare(MetadataContext context) {
        processor = context.getAppContext().getBeanFactory().tryGetBean(SqlTagProcessor.class, name);
        if(null == processor) {
            throw new SqlConfigException("Tag processor '" + name + "' not exists, check it : " + toString());
        }
        processor.prepareTag(context, this);
    }

    @Override
    protected void toString_(Appendable buf) throws IOException {
        buf.append("@").append(name).append("{").append(content).append("}");
    }

	@Override
    protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
        processor.processTag(context, this, stm, params);
    }
}
