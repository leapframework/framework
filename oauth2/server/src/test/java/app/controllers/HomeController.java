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

package app.controllers;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.i18n.MessageKey;
import leap.core.security.UserPrincipal;
import leap.core.security.annotation.AllowAnonymous;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.security.RSA;
import leap.oauth2.server.OAuth2Error;
import leap.oauth2.server.OAuth2Errors;
import leap.web.Request;
import leap.web.Response;
import leap.web.annotation.DefaultValue;

import java.util.Locale;

public class HomeController {
    protected @Inject AppConfig config;

    protected String key = null;

    public String checkLoginState(String username) {
        if(null == username) {
            return "OK";
        }

        UserPrincipal user = Request.current().getUser();
        if(user.getLoginName().equals(username)){
            return "OK";
        }else{
            return "Failed";
        }
    }

    @AllowAnonymous
    public String publickey() {
        if (Strings.isEmpty(key)) {
            // return an error public key in first time.
            key = Base64.encode(RSA.generateKeyPair().getPublicKey().getEncoded());
        } else {
            key = config.getProperty("oauth2.rs.rsaPublicKeyStr");
        }
        return key;
    }

    @AllowAnonymous
    public void oauth2ErrorResp(
            @DefaultValue("zh_CN") String locale,
            @DefaultValue(OAuth2Errors.ERROR_INVALID_REQUEST_KEY) String key,
            @DefaultValue("1") String[] args,
            @DefaultValue("401") int status,
            @DefaultValue(OAuth2Errors.ERROR_INVALID_REQUEST) String invalid,
            @DefaultValue("defaultValue") String defaultDesc,
            Request request, Response response) {
        Locale locale1;
        if (Strings.isNotEmpty(locale)) {
            locale1 = Locales.forName(locale);
        } else {
            locale1 = request.getLocale();
        }
        MessageKey key1 = OAuth2Errors.messageKey(locale1, key, args);
        OAuth2Error error = OAuth2Errors.oauth2Error(request, status, invalid, key1, defaultDesc);
        OAuth2Errors.response(response, error);
    }

}
