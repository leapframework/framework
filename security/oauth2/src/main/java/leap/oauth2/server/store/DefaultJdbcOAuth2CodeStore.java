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
import leap.oauth2.server.code.OAuth2AuthzCode;
import leap.oauth2.server.code.OAuth2AuthzCodeStore;
import leap.oauth2.server.code.SimpleOAuth2AuthzCode;
import leap.oauth2.server.entity.OAuth2CodeEntity;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.sql.SqlCommand;

public class DefaultJdbcOAuth2CodeStore extends AbstractJdbcOAuth2Store implements OAuth2AuthzCodeStore {
    
    private static final Log log = LogFactory.get(DefaultJdbcOAuth2CodeStore.class);
    
    public static final String LOAD_AUTHORIZATION_CODE_SQL_KEY     = "oauth2.as.loadAuthorizationCode";
    public static final String DELETE_AUTHORIZATION_CODE_SQL_KEY   = "oauth2.as.deleteAuthorizationCode";
    public static final String CLEANUP_AUTHORIZATION_CODES_SQL_KEY = "oauth2.as.cleanupAuthorizationCodes";
    
    protected SqlCommand loadAuthorizationCodeCommand;
    protected SqlCommand deleteAuthorizationCodeCommand;
    protected SqlCommand cleanupAuthorizationCodesCommand;
    
    @Override
    public void saveAuthorizationCode(OAuth2AuthzCode code) {
        dao.insert(createEntityFromAuthzCode(code));
    }

    @Override
    public OAuth2AuthzCode loadAuthorizationCode(String code) {
        OAuth2CodeEntity entity;
        if(null == loadAuthorizationCodeCommand) {
            entity = dao.find(OAuth2CodeEntity.class, code);
        }else{
            entity = dao.createQuery(OAuth2CodeEntity.class, loadAuthorizationCodeCommand).firstOrNull();
        }
        
        return null == entity ? null : createAuthzCodeFromEntity(entity);
    }

    @Override
    public OAuth2AuthzCode removeAuthorizationCode(String code) {
        OAuth2AuthzCode authzCode = loadAuthorizationCode(code);
        if(null == authzCode) {
            return null;
        }
        
        if(deleteAuthorizationCode(code)) {
            return authzCode;
        }else{
            return null;    
        }
    }

    @Override
    public void removeAuthorizationCode(OAuth2AuthzCode code) {
        deleteAuthorizationCode(code.getCode());
    }
    
    @Override
    public void cleanupAuthorizationCodes() {
        int result;
        if(null != cleanupAuthorizationCodesCommand) {
            result = dao.executeUpdate(cleanupAuthorizationCodesCommand, New.hashMap("now", new Date()));
        }else{
            result = dao.createCriteriaQuery(OAuth2CodeEntity.class).where("expiration <= :now", new Date()).delete();
        }
        log.info("Cleanup {} expired authorization codes", result);
    }

    protected boolean deleteAuthorizationCode(String code) {
        if(null == deleteAuthorizationCodeCommand) {
            return dao.delete(OAuth2CodeEntity.class, code) > 0;
        }else{
            return dao.executeUpdate(deleteAuthorizationCodeCommand,New.hashMap("code", code)) > 0;
        }
    }
    
    protected OAuth2AuthzCode createAuthzCodeFromEntity(OAuth2CodeEntity entity) {
        SimpleOAuth2AuthzCode code = new SimpleOAuth2AuthzCode();

        code.setCode(entity.getCode());
        code.setClientId(entity.getClientId());
        code.setUserId(entity.getUserId());
        code.setCreated(entity.getCreatedMs());
        code.setExpiresIn(entity.getExpiresIn());
        
        return code;
    }
    
    protected OAuth2CodeEntity createEntityFromAuthzCode(OAuth2AuthzCode code) {
        OAuth2CodeEntity entity = new OAuth2CodeEntity();
        
        entity.setCode(code.getCode());
        entity.setClientId(code.getClientId());
        entity.setUserId(code.getUserId());
        entity.setCreatedMs(code.getCreated());
        entity.setExpirationByExpiresIn(code.getExpiresIn());
        
        return entity;
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }
    
    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd = dmo.cmdCreateEntity(OAuth2CodeEntity.class);
        
        if(debug) {
            cmd.setUpgradeTable(true);
        }
        
        cmd.execute();
    }
    
    protected void resolveSqlCommands(Dao dao, OrmMetadata md) {
        loadAuthorizationCodeCommand    = md.tryGetSqlCommand(LOAD_AUTHORIZATION_CODE_SQL_KEY);
        deleteAuthorizationCodeCommand  = md.tryGetSqlCommand(DELETE_AUTHORIZATION_CODE_SQL_KEY);
        cleanupAuthorizationCodesCommand = md.tryGetSqlCommand(CLEANUP_AUTHORIZATION_CODES_SQL_KEY);
    }
    
}