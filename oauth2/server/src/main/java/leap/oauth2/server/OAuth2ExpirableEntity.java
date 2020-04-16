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
package leap.oauth2.server;

import leap.lang.expirable.TimeExpirable;

import java.sql.Timestamp;

public abstract class OAuth2ExpirableEntity implements OAuth2Entity {

    @Created
    protected Timestamp created;

    @Expiration
    protected Timestamp expiration;

    public Timestamp getCreated() {
        return created;
    }
    
    public long getCreatedMs() {
        return created.getTime();
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }
    
    public void setCreatedMs(long created) {
        this.created = new Timestamp(created);
    }

    public Timestamp getExpiration() {
        return expiration;
    }
    
    public long getExpirationMs() {
        return expiration.getTime();
    }
    
    public void setExpiration(Timestamp expiration) {
        this.expiration = expiration;
    }

    public void setExpirationMs(long expiration) {
        this.expiration = new Timestamp(expiration);
    }
    
    public void setExpirationByExpiresIn(int expiresIn) {
        this.setExpirationMs(created.getTime() + expiresIn * 1000L);
    }
    
    public int getExpiresIn() {
        return (int)((getExpirationMs() - created.getTime()) / 1000L);
    }

    public void setTimeExpirable(TimeExpirable te) {
        setCreatedMs(te.getCreated());
        setExpirationByExpiresIn(te.getExpiresIn());
    }
}
