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
import leap.oauth2.server.code.AuthzCode;
import leap.oauth2.server.code.AuthzCodeStore;
import leap.oauth2.server.code.SimpleAuthzCode;
import leap.oauth2.server.entity.AuthzCodeEntity;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.sql.SqlCommand;

public class DefaultJdbcAuthzCodeStore extends AbstractJdbcAuthzStore implements AuthzCodeStore {

    private static final Log log = LogFactory.get(DefaultJdbcAuthzCodeStore.class);

    public static final String LOAD_AUTHORIZATION_CODE_SQL_KEY     = "oauth2.as.loadAuthorizationCode";
    public static final String DELETE_AUTHORIZATION_CODE_SQL_KEY   = "oauth2.as.deleteAuthorizationCode";
    public static final String CLEANUP_AUTHORIZATION_CODES_SQL_KEY = "oauth2.as.cleanupAuthorizationCodes";

    protected SqlCommand loadAuthorizationCodeCommand;
    protected SqlCommand deleteAuthorizationCodeCommand;
    protected SqlCommand cleanupAuthorizationCodesCommand;

    @Override
    public void saveAuthorizationCode(AuthzCode code) {
        dao.insert(createEntityFromAuthzCode(code));
    }

    @Override
    public AuthzCode loadAuthorizationCode(String code) {
        AuthzCodeEntity entity;
        if(null == loadAuthorizationCodeCommand) {
            entity = dao.findOrNull(AuthzCodeEntity.class, code);
        }else{
            entity = dao.createQuery(AuthzCodeEntity.class, loadAuthorizationCodeCommand).firstOrNull();
        }

        return null == entity ? null : createAuthzCodeFromEntity(entity);
    }

    @Override
    public void removeAuthorizationCode(String code) {
        deleteAuthorizationCode(code);
    }

    @Override
    public AuthzCode removeAndLoadAuthorizationCode(String code) {
        AuthzCode authzCode = loadAuthorizationCode(code);

        if(null != authzCode) {
            removeAuthorizationCode(code);
        }

        return authzCode;
    }

    @Override
    public void cleanupAuthorizationCodes() {
        int result;
        if(null != cleanupAuthorizationCodesCommand) {
            result = dao.executeUpdate(cleanupAuthorizationCodesCommand, New.hashMap("now", new Date()));
        }else{
            result = dao.createCriteriaQuery(AuthzCodeEntity.class).where("expiration <= :now", new Date()).delete();
        }
        log.info("Cleanup {} expired authorization codes", result);
    }

    protected boolean deleteAuthorizationCode(String code) {
        if(null == deleteAuthorizationCodeCommand) {
            return dao.delete(AuthzCodeEntity.class, code) > 0;
        }else{
            return dao.executeUpdate(deleteAuthorizationCodeCommand,New.hashMap("code", code)) > 0;
        }
    }

    protected AuthzCode createAuthzCodeFromEntity(AuthzCodeEntity entity) {
        SimpleAuthzCode code = new SimpleAuthzCode();

        code.setCode(entity.getCode());
        code.setClientId(entity.getClientId());
        code.setUserId(entity.getUserId());
        code.setCreated(entity.getCreatedMs());
        code.setExpiresIn(entity.getExpiresIn());
        code.setExtendedParameters(entity.getExData());
        code.setSessionId(entity.getSessionId());

        return code;
    }

    protected AuthzCodeEntity createEntityFromAuthzCode(AuthzCode code) {
        AuthzCodeEntity entity = new AuthzCodeEntity();

        entity.setCode(code.getCode());
        entity.setSessionId(code.getSessionId());
        entity.setClientId(code.getClientId());
        entity.setUserId(code.getUserId());
        entity.setCreatedMs(code.getCreated());
        entity.setExpirationByExpiresIn(code.getExpiresIn());
        if(code.getExtendedParameters()!=null){
        	entity.setExData(code.getExtendedParameters());
        }

        return entity;
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }

    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd = dmo.cmdCreateEntity(AuthzCodeEntity.class);

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