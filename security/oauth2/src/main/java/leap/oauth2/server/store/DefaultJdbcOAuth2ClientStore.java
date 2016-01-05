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
import leap.lang.Strings;
import leap.lang.path.AntPathPattern;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.OAuth2ClientStore;
import leap.oauth2.server.client.SimpleOAuth2Client;
import leap.oauth2.server.entity.OAuth2ClientEntity;
import leap.orm.OrmMetadata;
import leap.orm.command.CreateEntityCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.sql.SqlCommand;

public class DefaultJdbcOAuth2ClientStore extends AbstractJdbcOAuth2Store implements OAuth2ClientStore {
    
    public static final String LOAD_CLIENT_SQL_KEY = "oauth2.as.loadClient";

    protected SqlCommand loadClientCommand;
    
    @Override
    public OAuth2Client loadClient(String clientId) {
        OAuth2ClientEntity entity;
        
        if(null == loadClientCommand) {
            entity = dao.findOrNull(OAuth2ClientEntity.class, clientId);
        }else{
            entity = dao.createQuery(OAuth2ClientEntity.class, loadClientCommand).firstOrNull();
        }
        
        return null == entity ? null : createAuthzClientFromEntity(entity);
    }
    
    protected OAuth2Client createAuthzClientFromEntity(OAuth2ClientEntity entity) {
        SimpleOAuth2Client client = new SimpleOAuth2Client();
        
        client.setId(entity.getId());
        client.setSecret(entity.getSecret());
        client.setRedirectUri(entity.getRedirectUri());

        if(!Strings.isEmpty(entity.getRedirectUriPattern())) {
            client.setRedirectUriPattern(new AntPathPattern(entity.getRedirectUriPattern()));
        }
        
        client.setAccessTokenExpires(entity.getAccessTokenExpires());
        client.setRefreshTokenExpires(entity.getRefreshTokenExpires());
        client.setAllowAuthorizationCode(entity.getAllowAuthorizationCode());
        client.setAllowRefreshToken(entity.getAllowRefreshToken());
        client.setEnabled(entity.isEnabled());
        
        return client;
    }

    @Override
    protected void init(AppConfig config) {
        createEntityMapping(dmo, config.isDebug());
        resolveSqlCommands(dao, dao.getOrmContext().getMetadata());
    }
    
    protected void createEntityMapping(Dmo dmo, boolean debug) {
        CreateEntityCommand cmd = dmo.cmdCreateEntity(OAuth2ClientEntity.class);
        
        if(debug) {
            cmd.setUpgradeTable(true);
        }
        
        cmd.execute();
    }
    
    protected void resolveSqlCommands(Dao dao, OrmMetadata md) {
        loadClientCommand = md.tryGetSqlCommand(LOAD_CLIENT_SQL_KEY);
    }
}