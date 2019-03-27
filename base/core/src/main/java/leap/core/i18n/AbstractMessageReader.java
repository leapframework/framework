/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.core.i18n;

import leap.core.annotation.Inject;
import leap.core.el.EL;
import leap.core.el.ExpressionLanguage;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public abstract class AbstractMessageReader implements MessageReader {

    protected final Log log = LogFactory.get(this.getClass());

    protected @Inject ExpressionLanguage el;

    protected final Message createMessage(Object source, String string) {
        return new Message(source, EL.createCompositeExpression(el, string));
    }

}