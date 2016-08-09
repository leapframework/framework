/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.mvc;

import leap.lang.http.HTTP;
import leap.lang.meta.annotation.TypeWrapper;
import leap.web.ResponseEntity;

@TypeWrapper
public class ApiResponse<T> implements ResponseEntity {

    //SUCCESS
    public static final ApiResponse OK         = of(HTTP.Status.OK,         null);
    public static final ApiResponse ACCEPTED   = of(HTTP.Status.ACCEPTED,   null);
    public static final ApiResponse NO_CONTENT = of(HTTP.Status.NO_CONTENT, null);

    //ERROR
    public static final ApiResponse NOT_FOUND       = err(HTTP.Status.NOT_FOUND,       "NotFound",       "The resource does not exists.");
    public static final ApiResponse NOT_IMPLEMENTED = err(HTTP.Status.NOT_IMPLEMENTED, "NotImplemented", "Not implemented operation.");

    public static ApiResponse ok(Object entity) {
        return of(HTTP.Status.OK, entity);
    }

    public static ApiResponse created(Object entity) {
        return of(HTTP.Status.CREATED, entity);
    }

    public static ApiResponse badRequest(String message) {
        return err(HTTP.Status.BAD_REQUEST, message);
    }

    public static ApiResponse err(HTTP.Status status, String message) {
        return of(status, new ApiError(message));
    }

    public static ApiResponse err(HTTP.Status status,String errorCode, String message){
        return of(status,new ApiError(errorCode,message));
    }

    public static ApiResponse of(HTTP.Status status, Object entity) {
        return new ApiResponse(status, entity);
    }

    public static ApiResponse of(Object entity) {
        if(null == entity) {
            return NOT_FOUND;
        }else{
            return ok(entity);
        }
    }

    protected final HTTP.Status status;
    protected final Object      entity;

    protected ApiResponse(HTTP.Status status, Object entity) {
        this.status = status;
        this.entity = entity;
    }

    @Override
    public HTTP.Status getStatus() {
        return status;
    }

    @Override
    public Object getEntity() {
        return entity;
    }

}