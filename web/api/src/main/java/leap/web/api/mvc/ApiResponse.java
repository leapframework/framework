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
import leap.lang.http.client.HttpHeaders;
import leap.lang.http.client.SimpleHttpHeaders;
import leap.lang.meta.annotation.TypeWrapper;
import leap.web.ResponseEntity;

@TypeWrapper
public class ApiResponse<T> implements ResponseEntity {

    //SUCCESS
    public static final ApiResponse OK         = of(HTTP.Status.OK,         null);
    public static final ApiResponse ACCEPTED   = of(HTTP.Status.ACCEPTED,   null);
    public static final ApiResponse NO_CONTENT = of(HTTP.Status.NO_CONTENT, null);

    //ERROR
    public static final ApiResponse NOT_FOUND       = err(HTTP.Status.NOT_FOUND,       "The resource does not exists.");
    public static final ApiResponse NOT_IMPLEMENTED = err(HTTP.Status.NOT_IMPLEMENTED, "Not implemented operation.");

    public static ApiResponse ok() {
        return OK;
    }

    public static ApiResponse ok(Object entity) {
        return of(HTTP.Status.OK, entity);
    }

    public static ApiResponse created(Object entity) {
        return of(HTTP.Status.CREATED, entity);
    }

    public static ApiResponse accepted() {
        return ACCEPTED;
    }

    public static ApiResponse noContent() {
        return NO_CONTENT;
    }

    public static ApiResponse badRequest(String message) {
        return err(HTTP.Status.BAD_REQUEST, message);
    }

    public static ApiResponse badRequest(String errorCode, String message) {
        return err(HTTP.Status.BAD_REQUEST, errorCode, message);
    }

    public static ApiResponse notFound(String message) {
        return err(HTTP.Status.NOT_FOUND, message);
    }

    public static ApiResponse notFound(String errorCode, String message) {
        return err(HTTP.Status.NOT_FOUND, errorCode, message);
    }

    public static ApiResponse err(String message) {
        return err(HTTP.Status.INTERNAL_SERVER_ERROR, message);
    }

    public static ApiResponse err(HTTP.Status status, String message) {
        return of(status, new ApiError(status.name(), message));
    }

    public static ApiResponse err(int status, String message) {
        return err(HTTP.Status.valueOf(status), message);
    }

    public static ApiResponse err(HTTP.Status status,String errorCode, String message){
        return of(status,new ApiError(errorCode,message));
    }

    public static ApiResponse err(int status,String errorCode, String message){
        return err(HTTP.Status.valueOf(status), errorCode, message);
    }

    public static ApiResponse of(HTTP.Status status, Object entity) {
        return new ApiResponse(status, entity);
    }

    public static ApiResponse of(int status, Object entity) {
        return new ApiResponse(HTTP.Status.valueOf(status), entity);
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
    protected final HttpHeaders headers = new SimpleHttpHeaders();

    protected ApiResponse(HTTP.Status status, Object entity) {
        this.status = status;
        this.entity = entity;
    }

    @Override
    public HTTP.Status getStatus() {
        return status;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Object getEntity() {
        return entity;
    }

    /**
     * Sets the header.
     */
    public ApiResponse<T> setHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    /**
     * Sets the header (same as {@link #setHeader(String, String)}.
     */
    public ApiResponse<T> withHeader(String name, String value) {
        headers.set(name, value);
        return this;
    }

    /**
     * Adds the header value.
     */
    public ApiResponse<T> addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

}