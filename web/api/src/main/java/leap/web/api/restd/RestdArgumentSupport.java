/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import leap.web.action.ArgumentBuilder;

public interface RestdArgumentSupport {

    default void processIdArgument(RestdContext context, RestdModel model, ArgumentBuilder a) {}

    default void processModelArgumentForCreate(RestdContext context, RestdModel model, ArgumentBuilder a){}

    default void processModelArgumentForUpdate(RestdContext context, RestdModel model, ArgumentBuilder a){}

    default void processModelArgumentForReplace(RestdContext context, RestdModel model, ArgumentBuilder a){}

    default void processArgument(RestdContext context, ArgumentBuilder a){}
}