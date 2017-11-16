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

package leap.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import leap.lang.Strings;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AEntity
public @interface RestEntity {
    /**
     * Name of the entity,
     */
    String value() default "";

    /**
     * Path relative to api's endpoint
     * default value is entity's name lowerUnderscore({@link Strings.lowerUnderscore})
     */
    String relativePath() default "";

    /**
     * The endpoint of the remote datasource, ie:http://xxx.domain.com/api/ <br/>
     * when the dataSource can't be found and the endpoint of the dataSource not empty,
     * fullUrl will be override by RestDataSource.getEndpoint()
     * @return
     */
    String endPoint() default "";

    /**
     * The target api's name
     */
    String dataSource() default "";

}