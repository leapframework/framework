/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.oauth2;

import leap.core.RequestContext;
import leap.core.i18n.MessageKey;

import java.util.UUID;

/**
 * Created by kael on 2017/3/1.
 */
public abstract class Oauth2MessageKey {
    protected Oauth2MessageKey() {}
    
    public static String INVALID_REQUEST_CLIENT_ID_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".client_id_required";
    public static String INVALID_REQUEST_USERNAME_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".username_required";
    public static String INVALID_REQUEST_PASSWORD_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".password_required";
    public static String INVALID_REQUEST_REDIRECT_URI_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".redirect_uri_required";
    public static String INVALID_REQUEST_CLIENT_SECRET_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".client_secret_required";
    public static String INVALID_REQUEST_INVALID_AUTHZ_HEADER = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".invalid_authz_header";
    public static String INVALID_REQUEST_INVALID_HTTP_METHOD = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".invalid_http_method";
    public static String INVALID_REQUEST_ACCESS_TOKEN_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".access_token_required";
    public static String INVALID_REQUEST_JWT_PUBLIC_KEY_UNDEFINED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".jwt_public_key_undefined";
    public static String INVALID_REQUEST_INVALID_USERNAME = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".invalid_username";
    public static String INVALID_REQUEST_INVALID_PASSWORD = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".invalid_password";
    public static String INVALID_REQUEST_INVALID_CLIENT = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".invalid_client";
    public static String INVALID_REQUEST_INVALID_CLIENT_SECRET = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".invalid_client_secret";
    public static String INVALID_REQUEST_AUTHORIZATION_CODE_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".authorization_code_required";
    public static String INVALID_REQUEST_REFRESH_TOKEN_REQUIRED = OAuth2Errors.ERROR_INVALID_REQUEST_KEY+".refresh_token_required";
    
    
    public static String ERROR_INVALID_GRANT_CLIENT_NOT_FOUND = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".client_not_found";
    public static String ERROR_INVALID_GRANT_REDIRECT_URI_INVALID = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".redirect_uri_invalid";
    public static String ERROR_INVALID_GRANT_AUTHORIZATION_CODE_NOT_ALLOW = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".authorization_code_not_allow";
    public static String ERROR_INVALID_GRANT_INVALID_AUTHORIZATION_CODE = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".invalid_authorization_code";
    public static String ERROR_INVALID_GRANT_AUTHORIZATION_CODE_EXPIRED = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".authorization_code_expired";
    public static String ERROR_INVALID_GRANT_USER_NOT_FOUND = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".user_not_found";
    public static String ERROR_INVALID_GRANT_INVALID_REFRESH_TOKEN = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".invalid_refresh_token";
    public static String ERROR_INVALID_GRANT_REFRESH_TOKEN_EXPIRED = OAuth2Errors.ERROR_INVALID_GRANT_KEY+".refresh_token_expired";
    
    public static String ERROR_UNSUPPORTED_GRANT_TYPE_TYPE = OAuth2Errors.ERROR_UNSUPPORTED_GRANT_TYPE+".type";
    
    public static String ERROR_INVALID_TOKEN_JWT_TOKEN_NOT_CONTAIN_USERNAME = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".jwt_token_not_contain_username";
    public static String ERROR_INVALID_TOKEN_JWT_TOKEN_NOT_CONTAIN_EXPIRES_IN = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".jwt_token_not_contain_expires_in";
    public static String ERROR_INVALID_TOKEN_JWT_TOKEN_EXPIRES_IN_FORMAT_ERROR = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".jwt_token_expires_in_format_error";
    public static String ERROR_INVALID_TOKEN_EXPIRED = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".token_expired";
    public static String ERROR_INVALID_TOKEN_INVALID = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".token_invalid";
    public static String ERROR_INVALID_TOKEN_NOT_FOR_CLIENT = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".token_not_for_client";
    public static String ERROR_INVALID_TOKEN_INVALID_USER = OAuth2Errors.ERROR_INVALID_TOKEN_KEY + ".invalid_user";
    
    public static MessageKey getMessageKey(String key, Object...args){
        return OAuth2Errors.messageKey(RequestContext.locale(),key,args);
    }
    public static MessageKey createRandomKey(){
        return OAuth2Errors.messageKey(RequestContext.locale(), UUID.randomUUID().toString());
    }
}
