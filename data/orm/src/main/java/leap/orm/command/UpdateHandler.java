/*
 *  Copyright 2020 the original author or authors.
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
 *
 */

package leap.orm.command;

import leap.orm.value.EntityWrapper;

import java.util.Map;
import java.util.function.Supplier;

public interface UpdateHandler {

    UpdateHandler NOP = new UpdateHandler() {
    };

    default void preProcessUpdateRecord(CommandContext context, EntityWrapper record) {

    }

    default void postProcessUpdateRecord(CommandContext context, Map<String, Object> record) {

    }

    default Integer handleUpdate(CommandContext context, Map<String, Object> record, Supplier<Integer> update) {
        return update.get();
    }
}
