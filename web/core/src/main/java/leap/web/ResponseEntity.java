/*
 * Copyright 2016 the original author or authors.
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
package leap.web;

import leap.lang.Args;
import leap.lang.http.HTTP;

public interface ResponseEntity {

    ResponseEntity NOT_FOUND  = of(HTTP.Status.NOT_FOUND, null);
    ResponseEntity NO_CONTENT = of(HTTP.Status.NO_CONTENT, null);

    /**
     * Creates a {@link ResponseEntity} of the given status and entity.
     */
    static ResponseEntity of(final HTTP.Status status,final Object entity) {
        Args.notNull(status);
        return new ResponseEntity() {
            @Override
            public HTTP.Status getStatus() {
                return status;
            }

            @Override
            public Object getEntity() {
                return entity;
            }
        };
    }

    /**
     * Creates a {@link ResponseEntity} of the given status and null entity.
     */
    static ResponseEntity of(final HTTP.Status status) {
        return of(status, null);
    }

    /**
     * Creates a {@link ResponseEntity} of OK status and the given entity.
     */
    static ResponseEntity ok(Object entity) {
        return of(HTTP.Status.OK, entity);
    }

    /**
     * Returns the http status.
     */
    default HTTP.Status getStatus() { return HTTP.Status.OK; }

    /**
     * Optional.
     *
     * Returns the entity.
     */
    default Object getEntity() { return null; }
}