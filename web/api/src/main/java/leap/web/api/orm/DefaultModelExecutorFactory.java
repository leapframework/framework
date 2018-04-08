/*
 * Copyright 2017 the original author or authors.
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

package leap.web.api.orm;

public class DefaultModelExecutorFactory implements ModelExecutorFactory {

    @Override
    public ModelCreateExecutor newCreateExecutor(ModelExecutorContext context) {
        return new DefaultModelCreateExecutor(context);
    }

    @Override
    public ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context) {
        return new DefaultModelUpdateExecutor(context);
    }

    @Override
    public ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context) {
        return new DefaultModelDeleteExecutor(context);
    }

    @Override
    public ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
        return new DefaultModelQueryExecutor(context);
    }

}
