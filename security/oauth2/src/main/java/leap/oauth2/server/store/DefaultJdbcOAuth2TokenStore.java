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
package leap.oauth2.server.store;

import java.util.Date;

import leap.core.AppConfig;
import leap.lang.New;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.server.entity.OAuth2AccessTokenEntity;
import leap.oauth2.server.entity.OAuth2RefreshTokenEntity;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2RefreshToken;
import leap.oauth2.server.token.OAuth2TokenStore;
import leap.oauth2.server.token.SimpleOAuth2AccessToken;
import leap.oauth2.server.token.SimpleAuthzRefreshToken;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.sql.SqlCommand;

public class DefaultJdbcOAuth2TokenStore extends AbstractJdbcOAuth2Store implements OAuth2TokenStore {
    
    private static final Log log = LogFactory.get(DefaultJdbcOAuth2TokenStore.class);
    
    public static final String CLEANUP_ACCESS_TOKENS_SQL_KEY = "oauth2.as.cleanupAccessTokens";
    public static final String CLEANUP_REFRESH_TOKENS_SQL_KEY = "oauth2.as.cleanupRefreshTokens";

    protected SqlCommand cleanupAccessTokensCommand;
    protected SqlCommand cleanupRefreshTokensCommand;
    
    @Override
    public void saveAccessToken(OAuth2AccessToken token) {
        dao.insert(createEntityFromAccessToken(token));
    }
    
    @Override
    public void saveRefreshToken(OAuth2RefreshToken token) {
        dao.insert(createEnttiyFromRefreshToken(token));
    }

    @Override
    public OAuth2AccessToken loadAccessToken(String accessToken) {
        OAuth2AccessTokenEntity entity = dao.findOrNull(OAuth2AccessTokenEntity.class, accessToken);
        
        return null == entity ? null : createAccesTokenFromEntity(entity);
    }
    
    @Override
    public OAuth2RefreshToken loadRefreshToken(String refreshToken) {
        OAuth2RefreshTokenEntity entity = dao.findOrNull(OAuth2RefreshTokenEntity.class, refreshToken);
        
        return null == entity ? null : createRefreshTokenFromEntity(entity);
    }
    
    @Override
    public void removeAccessToken(String accessToken) {
        dao.delete(OAuth2AccessTokenEntity.class, accessToken);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        dao.delete(OAuth2RefreshTokenEntity.class, refreshToken);
    }
    
    @Override
    public void cleanupTokens() {
        Date now = new Date();
        
        cleanupAccessTokens(now);
        cleanupRefreshTokens(now);
    }
    
    protected void cleanupAccessTokens(Date now) {
        int result;
        if(null != cleanupAccessTokensCommand) {
            result = dao.executeUpdate(cleanupAccessTokensCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(OAuth2AccessTokenEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired access tokens", result);
    }
    
    protected void cleanupRefreshTokens(Date now) {
        int result;
        if(null != cleanupRefreshTokensCommand) {
            result = dao.executeUpdate(cleanupRefreshTokensCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(OAuth2RefreshTokenEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired refresh tokens", result);
    }

    protected OAuth2AccessTokenEntity createEntityFromAccessToken(OAuth2AccessToken token) {
        OAuth2AccessTokenEntity entity = new OAuth2AccessTokenEntity();
        
        entity.setToken(token.getToken());
        entity.setClientId(token.getClientId());
        entity.setUserId(token.getUserId());
        entity.setRefreshToken(token.getRefreshToken());
        entity.setTimeExpirable(token);
        
        return entity;
    }
    
    protected OAuth2RefreshTokenEntity createEnttiyFromRefreshToken(OAuth2RefreshToken token) {
        OAuth2RefreshTokenEntity entity = new OAuth2RefreshTokenEntity();
        
        entity.setToken(token.getToken());
        entity.setClientId(token.getClientId());
        entity.setUserId(token.getUserId());
        entity.setScope(token.getScope());
        entity.setTimeExpirable(token);
        
        return entity;
    }
    
    protected OAuth2AccessToken createAccesTokenFromEntity(OAuth2AccessTokenEntity entity) {
        SimpleOAuth2AccessToken token = new SimpleOAuth2AccessToken();

        token.setToken(entity.getToken());
        token.setClientId(entity.getClientId());
        token.setUserId(entity.getUserId());
        token.setRefreshToken(entity.getRefreshToken());
        token.setScope(entity.getScope());
        token.setCreated(entity.getCreatedMs());
        token.setExpiresIn(entity.getExpiresIn());
        
        return token;
    }
    
    protected OAuth2RefreshToken createRefreshTokenFromEntity(OAuth2RefreshTokenEntity entity) {
        SimpleAuthzRefreshToken token = new SimpleAuthzRefreshToken();

        token.setToken(entity.getToken());
        token.setClientId(entity.getClientId());
        token.setUserId(entity.getUserId());
        token.setScope(entity.getScope());
        token.setCreated(entity.getCreatedMs());
        token.setExpiresIn(entity.getExpiresIn());
        
        return token;
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }
    
    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd1 = dmo.cmdCreateEntity(OAuth2AccessTokenEntity.class);
        CreateEntityCommand cmd2 = dmo.cmdCreateEntity(OAuth2RefreshTokenEntity.class);

        if(debug) {
            cmd1.setUpgradeTable(true);
            cmd2.setUpgradeTable(true);
        }
        
        cmd1.execute();
        cmd2.execute();
    }
    
    protected void resolveSqlCommands(Dao dao, OrmMetadata md) {
        //TODO : 
        cleanupAccessTokensCommand  = md.tryGetSqlCommand(CLEANUP_ACCESS_TOKENS_SQL_KEY);
        cleanupRefreshTokensCommand = md.tryGetSqlCommand(CLEANUP_REFRESH_TOKENS_SQL_KEY);
    }
}
