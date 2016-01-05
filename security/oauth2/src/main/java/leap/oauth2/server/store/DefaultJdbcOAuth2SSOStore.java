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

import leap.core.AppConfig;
import leap.lang.New;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.server.entity.OAuth2SSOLoginEntity;
import leap.oauth2.server.entity.OAuth2SSOSessionEntity;
import leap.oauth2.server.sso.*;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.sql.SqlCommand;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultJdbcOAuth2SSOStore extends AbstractJdbcOAuth2Store implements OAuth2SSOStore {
    
    private static final Log log = LogFactory.get(DefaultJdbcOAuth2SSOStore.class);
    
    public static final String CLEANUP_SSO_LOGINS_SQL_KEY     = "oauth2.as.cleanupSSOLogins";
    public static final String CLEANUP_SSO_SESSIONS_SQL_KEY   = "oauth2.as.cleanupSSOSessions";
    public static final String LOAD_SESSION_BY_TOKEN_SQL_KEY  = "oauth2.as.loadSSOSessionByToken";
    public static final String LOAD_LOGINS_IN_SESSION_SQL_KEY = "oauth2.as.loadSSOLoginsInSession";

    protected SqlCommand loadSessionByTokenCommand;
    protected SqlCommand loadLoginsInSessionCommand;
    protected SqlCommand cleanupLoginsCommand;
    protected SqlCommand cleanupSessionsCommand;

    @Override
    public OAuth2SSOSession loadSessionByToken(String username, String token) {
        OAuth2SSOSessionEntity session = null;
        if(null != loadSessionByTokenCommand) {
            session = dao.createQuery(OAuth2SSOSessionEntity.class, loadSessionByTokenCommand).singleOrNull();
        }else{
            session = dao.createCriteriaQuery(OAuth2SSOSessionEntity.class)
                         .where("token = ? and user_name = ? and expiration > ?", new Object[]{token, username, new Date()})
                         .singleOrNull();
        }

        if(null == session) {
            return null;
        }

        return createSessionFromEntity(session);
    }

    @Override
    public List<OAuth2SSOLogin> loadLoginsInSession(OAuth2SSOSession session) {
        List<OAuth2SSOLoginEntity> entities = null;

        if(null != loadLoginsInSessionCommand) {
            entities = dao.createQuery(OAuth2SSOLoginEntity.class, loadLoginsInSessionCommand).list();
        }else{
            entities = dao.createCriteriaQuery(OAuth2SSOLoginEntity.class).where("session_id = ?", session.getId()).list();
        }

        List<OAuth2SSOLogin> logins = new ArrayList<>();
        for(OAuth2SSOLoginEntity entity : entities) {
            logins.add(createLoginFromEntity(entity));
        }
        return logins;
    }

    @Override
    public void saveSession(OAuth2SSOSession session, OAuth2SSOLogin initialLogin) {
        OAuth2SSOSessionEntity sessionEntity = createEntityFromSession(session);
        OAuth2SSOLoginEntity loginEntity = createEntityFromLogin(session, initialLogin);

        dao.doTransaction((s) -> {
            dao.insert(sessionEntity);
            dao.insert(loginEntity);
        });
    }

    @Override
    public void saveLogin(OAuth2SSOSession session, OAuth2SSOLogin newlogin) {
        OAuth2SSOLoginEntity entity = createEntityFromLogin(session, newlogin);
        dao.insert(entity);
    }

    @Override
    public void cleanupSSO() {
        Date now = new Date();

        cleanupLogins(now);
        cleanupSessions(now);
    }

    protected OAuth2SSOSession createSessionFromEntity(OAuth2SSOSessionEntity entity) {
        SimpleOAuth2SSOSession session = new SimpleOAuth2SSOSession();

        session.setId(entity.getId());
        session.setUserId(entity.getUserId());
        session.setUsername(entity.getUsername());
        session.setToken(entity.getToken());
        session.setCreated(entity.getCreatedMs());
        session.setExpiresIn(entity.getExpiresIn());

        return session;
    }

    protected OAuth2SSOLogin createLoginFromEntity(OAuth2SSOLoginEntity entity) {
        SimpleOAuth2SSOLogin login = new SimpleOAuth2SSOLogin();

        login.setLoginTime(entity.getLoginTime().getTime());
        login.setLogoutUri(entity.getLogoutUri());
        login.setClientId(entity.getClientId());
        login.setInitial(entity.isInitial());

        return login;
    }

    protected OAuth2SSOSessionEntity createEntityFromSession(OAuth2SSOSession session) {
        OAuth2SSOSessionEntity entity = new OAuth2SSOSessionEntity();

        entity.setId(session.getId());
        entity.setUserId(session.getUserId());
        entity.setUsername(session.getUsername());
        entity.setToken(session.getToken());
        entity.setTimeExpirable(session);

        return entity;
    }

    protected OAuth2SSOLoginEntity createEntityFromLogin(OAuth2SSOSession session, OAuth2SSOLogin login) {
        OAuth2SSOLoginEntity entity = new OAuth2SSOLoginEntity();

        entity.setSessionId(session.getId());
        entity.setLoginTime(new Timestamp(login.getLoginTime()));
        entity.setLogoutUri(login.getLogoutUri());
        entity.setClientId(login.getClientId());
        entity.setInitial(login.isInitial());
        entity.setExpiration(session);

        return entity;
    }

    protected void cleanupLogins(Date now) {
        int result;
        if(null != cleanupLoginsCommand) {
            result = dao.executeUpdate(cleanupLoginsCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(OAuth2SSOLoginEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired sso logins", result);
    }

    protected void cleanupSessions(Date now) {
        int result;
        if(null != cleanupSessionsCommand) {
            result = dao.executeUpdate(cleanupSessionsCommand, New.hashMap("now",now));
        }else{
            result = dao.createCriteriaQuery(OAuth2SSOSessionEntity.class).where("expiration <= :now",now).delete();
        }
        log.info("Cleanup {} expired sso sessions", result);
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }

    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd1 = dmo.cmdCreateEntity(OAuth2SSOSessionEntity.class);
        CreateEntityCommand cmd2 = dmo.cmdCreateEntity(OAuth2SSOLoginEntity.class);

        if(debug) {
            cmd1.setUpgradeTable(true);
            cmd2.setUpgradeTable(true);
        }

        cmd1.execute();
        cmd2.execute();
    }

    protected void resolveSqlCommands(Dao dao, OrmMetadata md) {
        loadSessionByTokenCommand  = md.tryGetSqlCommand(LOAD_SESSION_BY_TOKEN_SQL_KEY);
        loadLoginsInSessionCommand = md.tryGetSqlCommand(LOAD_LOGINS_IN_SESSION_SQL_KEY);
        cleanupLoginsCommand       = md.tryGetSqlCommand(CLEANUP_SSO_LOGINS_SQL_KEY);
        cleanupSessionsCommand     = md.tryGetSqlCommand(CLEANUP_SSO_SESSIONS_SQL_KEY);
    }
}
