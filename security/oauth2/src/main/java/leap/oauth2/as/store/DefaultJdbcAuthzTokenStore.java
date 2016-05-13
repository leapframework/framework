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
package leap.oauth2.as.store;

import leap.core.AppConfig;
import leap.lang.New;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.as.entity.AuthzAccessTokenEntity;
import leap.oauth2.as.entity.AuthzLoginTokenEntity;
import leap.oauth2.as.entity.AuthzRefreshTokenEntity;
import leap.oauth2.as.token.*;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.sql.SqlCommand;
import leap.web.security.user.UserDetails;

import java.util.Date;

public class DefaultJdbcAuthzTokenStore extends AbstractJdbcAuthzStore implements AuthzTokenStore {
    
    private static final Log log = LogFactory.get(DefaultJdbcAuthzTokenStore.class);
    
    public static final String CLEANUP_ACCESS_TOKENS_SQL_KEY  = "oauth2.as.cleanupAccessTokens";
    public static final String CLEANUP_REFRESH_TOKENS_SQL_KEY = "oauth2.as.cleanupRefreshTokens";
    public static final String CLEANUP_LOGIN_TOKENS_SQL_KEY   = "oauth2.as.cleanupLoginTokens";

    protected SqlCommand cleanupAccessTokensCommand;
    protected SqlCommand cleanupRefreshTokensCommand;
    protected SqlCommand cleanupLoginTokensCommand;
    
    @Override
    public void saveAccessToken(AuthzAccessToken token) {
        dao.insert(createEntityFromAccessToken(token));
    }
    
    @Override
    public void saveRefreshToken(AuthzRefreshToken token) {
        dao.insert(createEnttiyFromRefreshToken(token));
    }

    @Override
    public void saveLoginToken(AuthzLoginToken token) {
        dao.insert(createEntityFromLoginToken(token));
    }

    @Override
    public AuthzAccessToken loadAccessToken(String accessToken) {
        AuthzAccessTokenEntity entity = dao.findOrNull(AuthzAccessTokenEntity.class, accessToken);
        
        return null == entity ? null : createAccesTokenFromEntity(entity);
    }
    
    @Override
    public AuthzRefreshToken loadRefreshToken(String refreshToken) {
        AuthzRefreshTokenEntity entity = dao.findOrNull(AuthzRefreshTokenEntity.class, refreshToken);
        
        return null == entity ? null : createRefreshTokenFromEntity(entity);
    }

    @Override
    public AuthzLoginToken loadLoginToken(String loginToken) {
        AuthzLoginTokenEntity entity = dao.findOrNull(AuthzLoginTokenEntity.class, loginToken);

        return null == entity ? null : createLoginTokenFromEntity(entity);
    }

    @Override
    public void removeAccessToken(String accessToken) {
        dao.delete(AuthzAccessTokenEntity.class, accessToken);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        dao.delete(AuthzRefreshTokenEntity.class, refreshToken);
    }

    @Override
    public void removeLoginToken(String loginToken) {
        dao.delete(AuthzLoginTokenEntity.class, loginToken);
    }

    @Override
    public AuthzLoginToken removeAndLoadLoginToken(String loginToken) {
        AuthzLoginToken token = loadLoginToken(loginToken);

        if(null != token) {
            removeLoginToken(token.getToken());
        }

        return token;
    }

    @Override
    public void cleanupTokens() {
        Date now = new Date();
        
        cleanupAccessTokens(now);
        cleanupRefreshTokens(now);
        cleanupLoginTokens(now);
    }
    
    protected void cleanupAccessTokens(Date now) {
        int result;
        if(null != cleanupAccessTokensCommand) {
            result = dao.executeUpdate(cleanupAccessTokensCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(AuthzAccessTokenEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired access tokens", result);
    }
    
    protected void cleanupRefreshTokens(Date now) {
        int result;
        if(null != cleanupRefreshTokensCommand) {
            result = dao.executeUpdate(cleanupRefreshTokensCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(AuthzRefreshTokenEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired refresh tokens", result);
    }

    protected void cleanupLoginTokens(Date now) {
        int result;
        if(null != cleanupLoginTokensCommand) {
            result = dao.executeUpdate(cleanupLoginTokensCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(AuthzLoginTokenEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired login tokens", result);
    }

    protected AuthzAccessTokenEntity createEntityFromAccessToken(AuthzAccessToken token) {
        AuthzAccessTokenEntity entity = new AuthzAccessTokenEntity();
        
        entity.setToken(token.getToken());
        entity.setClientId(token.getClientId());
        entity.setUserId(token.getUserId());
        entity.setRefreshToken(token.getRefreshToken());
        entity.setTimeExpirable(token);
        entity.setScope(token.getScope());
        
        return entity;
    }
    
    protected AuthzRefreshTokenEntity createEnttiyFromRefreshToken(AuthzRefreshToken token) {
        AuthzRefreshTokenEntity entity = new AuthzRefreshTokenEntity();
        
        entity.setToken(token.getToken());
        entity.setClientId(token.getClientId());
        entity.setUserId(token.getUserId());
        entity.setScope(token.getScope());
        entity.setTimeExpirable(token);
        entity.setScope(token.getScope());
        
        return entity;
    }
    
    protected AuthzAccessToken createAccesTokenFromEntity(AuthzAccessTokenEntity entity) {
        SimpleAuthzAccessToken token = new SimpleAuthzAccessToken();
        // add user login name
        UserDetails ud = sc.getUserStore().loadUserDetailsById(entity.getUserId());
        if(ud != null){
            token.setUsername(ud.getLoginName());
        }
        token.setToken(entity.getToken());
        token.setClientId(entity.getClientId());
        token.setUserId(entity.getUserId());
        token.setRefreshToken(entity.getRefreshToken());
        token.setScope(entity.getScope());
        token.setCreated(entity.getCreatedMs());
        token.setExpiresIn(entity.getExpiresIn());
        
        return token;
    }
    
    protected AuthzRefreshToken createRefreshTokenFromEntity(AuthzRefreshTokenEntity entity) {
        SimpleAuthzRefreshToken token = new SimpleAuthzRefreshToken();

        token.setToken(entity.getToken());
        token.setClientId(entity.getClientId());
        token.setUserId(entity.getUserId());
        token.setScope(entity.getScope());
        token.setCreated(entity.getCreatedMs());
        token.setExpiresIn(entity.getExpiresIn());
        
        return token;
    }

    protected AuthzLoginToken createLoginTokenFromEntity(AuthzLoginTokenEntity entity) {
        SimpleAuthzLoginToken token = new SimpleAuthzLoginToken();
        token.setToken(entity.getToken());
        token.setClientId(entity.getClientId());
        token.setUserId(entity.getUserId());
        token.setCreated(entity.getCreatedMs());
        token.setExpiresIn(entity.getExpiresIn());

        return token;
    }

    protected AuthzLoginTokenEntity createEntityFromLoginToken(AuthzLoginToken token) {
        AuthzLoginTokenEntity entity = new AuthzLoginTokenEntity();

        entity.setToken(token.getToken());
        entity.setClientId(token.getClientId());
        entity.setUserId(token.getUserId());
        entity.setCreatedMs(token.getCreated());
        entity.setExpirationByExpiresIn(token.getExpiresIn());

        return entity;
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }
    
    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd1 = dmo.cmdCreateEntity(AuthzAccessTokenEntity.class);
        CreateEntityCommand cmd2 = dmo.cmdCreateEntity(AuthzRefreshTokenEntity.class);
        CreateEntityCommand cmd3 = dmo.cmdCreateEntity(AuthzLoginTokenEntity.class);

        if(debug) {
            cmd1.setUpgradeTable(true);
            cmd2.setUpgradeTable(true);
            cmd3.setUpgradeTable(true);
        }
        
        cmd1.execute();
        cmd2.execute();
        cmd3.execute();
    }
    
    protected void resolveSqlCommands(Dao dao, OrmMetadata md) {
        //TODO : 
        cleanupAccessTokensCommand  = md.tryGetSqlCommand(CLEANUP_ACCESS_TOKENS_SQL_KEY);
        cleanupRefreshTokensCommand = md.tryGetSqlCommand(CLEANUP_REFRESH_TOKENS_SQL_KEY);
        cleanupLoginTokensCommand   = md.tryGetSqlCommand(CLEANUP_LOGIN_TOKENS_SQL_KEY);
    }
}
