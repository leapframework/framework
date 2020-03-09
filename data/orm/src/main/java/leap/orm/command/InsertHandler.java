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

import leap.core.jdbc.PreparedStatementHandler;
import leap.orm.sql.SqlCommand;
import leap.orm.value.EntityWrapper;

import java.util.Map;

public interface InsertHandler {

    InsertHandler NOP = new InsertHandler() {
    };

    default void handleEntity(CommandContext context, EntityWrapper entity) {

    }

    default SqlCommand handleNewPrimaryCommand(CommandContext context, EntityWrapper entity, String[] fields) {
        return null;
    }

    default SqlCommand handleNewSecondaryCommand(CommandContext context, EntityWrapper entity, String[] fields) {
        return null;
    }

    default int handleExecutePrimaryCommand(CommandContext context, SqlCommand command, Map<String, Object> map, PreparedStatementHandler psHandler) {
        return -1;
    }

    default int handleExecuteSecondaryCommand(CommandContext context, SqlCommand command, Map<String, Object> map) {
        return -1;
    }
}
