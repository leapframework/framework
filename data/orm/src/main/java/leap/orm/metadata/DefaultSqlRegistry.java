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

package leap.orm.metadata;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.exception.ObjectExistsException;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSqlRegistry implements SqlRegistry {

    protected final Map<String, SqlFragment>   fragmentsMap = new ConcurrentHashMap<>();
    protected final Map<String, TypedCommands> commandsMap  = new ConcurrentHashMap<>();

    @Override
    public void addSqlFragment(String key, SqlFragment fragment) throws ObjectExistsException {
        Args.notEmpty(key,"fragment key");
        Args.notNull(fragment,"sql fragment");
        if(fragmentsMap.containsKey(key)){
            throw new ObjectExistsException("The sql fragment '" + key + "' already exists");
        }
        fragmentsMap.put(key, fragment);
    }

    @Override
    public SqlFragment tryGetSqlFragment(String key) {
        return fragmentsMap.get(key);
    }

    @Override
    public void addSqlCommand(String key, String dbType, SqlCommand cmd) throws ObjectExistsException {
        TypedCommands commands = commandsMap.get(key);
        if(null == commands) {
            commands = new TypedCommands();
            commandsMap.put(key, commands);
        }

        if(Strings.isEmpty(dbType)) {
            if(null != commands.untyped) {
                throw new ObjectExistsException("The sql command '" + key + "' already exists!");
            }
            commands.untyped = cmd;
        }else {
            if(commands.typed.containsValue(dbType.toLowerCase())) {
                throw new ObjectExistsException("The sql command '" + key + "' for db type '" + dbType + "' already exists!");
            }
            commands.typed.put(dbType.toLowerCase(), cmd);
        }
    }

    @Override
    public SqlCommand tryGetSqlCommand(String key, String dbType) {
        TypedCommands commands = commandsMap.get(key);
        if(null == commands) {
            return null;
        }

        if(Strings.isEmpty(dbType)) {
            return commands.untyped;
        }else {
            SqlCommand command = commands.typed.get(dbType.toLowerCase());
            if(null == command) {
                command = commands.untyped;
            }
            return command;
        }
    }

    @Override
    public SqlCommand removeSqlCommand(String key, String dbType) {
        TypedCommands commands = commandsMap.get(key);
        if(null == commands) {
            return null;
        }

        if(Strings.isEmpty(dbType)) {
            SqlCommand command = commands.untyped;
            commands.untyped = null;
            return command;
        }else {
            return commands.typed.remove(dbType.toLowerCase());
        }
    }

    protected static final class TypedCommands {
        SqlCommand              untyped;
        Map<String, SqlCommand> typed = new HashMap<>();
    }

}
