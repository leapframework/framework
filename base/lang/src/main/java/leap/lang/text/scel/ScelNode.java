/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.lang.text.scel;

import java.util.List;

public class ScelNode {
    protected final ScelToken      token;
    protected final String         literal;
    protected final boolean        quoted;
    protected final List<ScelNode> values;

    public ScelNode(ScelToken token, String literal) {
        this(token, literal, false);
    }

    public ScelNode(ScelToken token, String literal, List<ScelNode> values) {
        this(token, literal, false, values);
    }

    public ScelNode(ScelToken token, String literal, boolean quoted) {
        this(token, literal, quoted, null);
    }

    public ScelNode(ScelToken token, String literal, boolean quoted, List<ScelNode> values) {
        this.token = token;
        this.literal = literal;
        this.quoted = quoted;
        this.values  = values;
    }

    public ScelToken token() {
        return token;
    }

    public String literal() {
        return literal;
    }

    public List<ScelNode> values() {
        return values;
    }

    public boolean isQuoted() {
        return quoted;
    }

    public boolean isParen() {
        return token == ScelToken.LPAREN || token == ScelToken.RPAREN;
    }

    public boolean isAnd() {
        return token == ScelToken.AND;
    }

    public boolean isOr() {
        return token == ScelToken.OR;
    }

    public boolean isNull() {
        return token == ScelToken.NULL;
    }

    public boolean isNot() {
        return token == ScelToken.NOT;
    }
}