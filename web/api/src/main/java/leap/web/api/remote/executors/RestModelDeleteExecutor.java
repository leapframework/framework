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

package leap.web.api.remote.executors;

import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.orm.DeleteOneResult;
import leap.web.api.orm.ModelDeleteExecutor;
import leap.web.api.orm.ModelExecutorBase;
import leap.web.api.orm.ModelExecutorContext;

public class RestModelDeleteExecutor extends ModelExecutorBase implements ModelDeleteExecutor {

    public RestModelDeleteExecutor(ModelExecutorContext context) {
        super(context);
    }

    @Override
    public DeleteOneResult deleteOne(Object id, DeleteOptions options) {
        return null;
    }
}
