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
package leap.oauth2.wac.token;

import javax.servlet.http.Cookie;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.store.JdbcStore;
import leap.oauth2.as.store.AbstractJdbcAuthzStore;
import leap.oauth2.wac.OAuth2WebAppConfig;
import leap.oauth2.wac.OAuth2AccessToken;
import leap.oauth2.wac.entity.WacAccessTokenEntity;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.web.Request;
import leap.web.Response;
import leap.web.config.WebConfig;
import leap.web.cookie.AbstractCookieBean;

//TODO : cleanup when refresh token expired.
public class JdbcWacTokenStore extends AbstractJdbcAuthzStore implements WacTokenStore, JdbcStore {
    
    protected @Inject WebConfig          wc;
    protected @Inject OAuth2WebAppConfig config;
    
    protected AbstractCookieBean cookieBean;

    protected AbstractCookieBean getCookieBean() {
        if(null == cookieBean) {
            cookieBean = new AccessTokenCookieBean(wc);
        }
        return cookieBean;
    }
    
    @Override
    public void saveAccessToken(Request request, Response response, OAuth2AccessToken at) {
        WacAccessTokenEntity entity = createEntityFromAccesstoken(at);
        
        dao.insert(entity);
            
        getCookieBean().setCookie(request, request.response(), entity.getId());
    }

    @Override
    public OAuth2AccessToken loadAccessToken(Request request) {
        Cookie cookie = getCookieBean().getCookie(request);
        if(null == cookie) {
            return null;
        }
        
        WacAccessTokenEntity entity = dao.find(WacAccessTokenEntity.class, cookie.getValue());
        if(null == entity) {
            return null;
        }
        
        return createAccessTokenFromEntity(entity);
    }
    
    @Override
    public void removeAccessToken(Request request, OAuth2AccessToken at) {
        dao.createCriteriaQuery(WacAccessTokenEntity.class).where("token = ?",at.getToken()).delete();
    }

    protected OAuth2AccessToken createAccessTokenFromEntity(WacAccessTokenEntity entity) {
        SimpleWacAccessToken token = new SimpleWacAccessToken();

        token.setToken(entity.getToken());
        token.setUserId(entity.getUserId());
        token.setRefreshToken(entity.getRefreshToken());
        token.setCreated(entity.getCreatedMs());
        token.setExpiresIn(entity.getExpiresIn());
        
        return token;
    }
    
    protected WacAccessTokenEntity createEntityFromAccesstoken(OAuth2AccessToken token) {
        WacAccessTokenEntity entity = new WacAccessTokenEntity();
        
        entity.setToken(token.getToken());
        entity.setUserId(token.getUserId());
        entity.setRefreshToken(token.getRefreshToken());
        entity.setTimeExpirable(token);
        
        return entity;
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }
    
    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd = dmo.cmdCreateEntity(WacAccessTokenEntity.class);

        if(debug) {
            cmd.setUpgradeTable(true);
        }
        
        cmd.execute();
    }
    
    protected void resolveSqlCommands(Dao dao, OrmMetadata md) {
        //TODO : 
    }

    protected class AccessTokenCookieBean extends AbstractCookieBean {
        
        public AccessTokenCookieBean(WebConfig c) {
            this.webConfig = c;
        }

        @Override
        public String getCookieName() {
            return config.getAccessTokenCookieName();
        }

        @Override
        public boolean isCookieHttpOnly() {
            return true;
        }
    }

}