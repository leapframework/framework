/*
 * Copyright 2015 the original author or authors.
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
package leap.oauth2.server;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import leap.lang.enums.Bool;
import leap.orm.annotation.ADomain;
import leap.orm.annotation.Column;
import leap.orm.annotation.ColumnType;

public interface OAuth2Entity {
    
    @ADomain(length=190)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Token {}

    @ADomain(length=1000)
    @Retention(RetentionPolicy.RUNTIME)
    @interface LongToken {}
    
    @ADomain(column="user_id", length=38)
    @Retention(RetentionPolicy.RUNTIME)
    @interface UserId {}

    @ADomain(column="user_name", length=50)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Username {}
    
    @ADomain(column="client_id", length=50)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ClientId {}
    
    @ADomain(type=ColumnType.TIMESTAMP, nullable=Bool.FALSE, order=Column.ORDER_LAST, defaultValue = "${env.timestamp}")
    @Retention(RetentionPolicy.RUNTIME)
    @interface Created {}
    
    @ADomain(type=ColumnType.TIMESTAMP, nullable=Bool.FALSE, order=Column.ORDER_LAST)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Expiration {}

    @ADomain(length=1000)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Scope {}

    @ADomain(length=1000)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Uri {}
}