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
package leap.htpl.interceptor;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Node;

public interface ProcessInterceptor {

    default void preProcessDocument(HtplEngine engine, HtplDocument document) throws Throwable {

    }

    default void postProcessDocument(HtplEngine engine, HtplDocument document) throws Throwable {

    }

	default void preProcessNode(HtplEngine engine, HtplDocument document, Node node) throws Throwable {

    }

	default Node postProcessNode(HtplEngine engine, HtplDocument document, Node node, Node processed) throws Throwable {
        return processed;
    }

}