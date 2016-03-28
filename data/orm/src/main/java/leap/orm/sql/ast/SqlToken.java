/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.sql.ast;

import leap.orm.sql.parser.Token;

import java.io.IOException;

public class SqlToken extends SqlNode {

    private final Token  token;
    private final String text;

    public SqlToken(Token token, String text) {
        this.token = token;
        this.text  = text;
    }

    public Token getToken() {
        return token;
    }

    public String getText() {
        return text;
    }

    @Override
    protected void toString_(Appendable buf) throws IOException {
        buf.append(text);
    }
}
