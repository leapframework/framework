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
package leap.lang.expirable;


public class TimeExpirableMs implements TimeExpirable {
    
    protected long created;
    protected int  expiresIn;
    
    public TimeExpirableMs() {
        super();
    }

    public TimeExpirableMs(int expiresInMs) {
        this(System.currentTimeMillis(), expiresInMs);
    }
    
    public TimeExpirableMs(long created, int expiresInMs) {
        this.created = created;
        this.expiresIn = expiresInMs;
    }
    
    @Override
    public int getExpiresIn() {
        return expiresIn/1000;
    }

    @Override
    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setExpiresIn(int millSeconds) {
        this.expiresIn = millSeconds;
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() - created >= expiresIn;
    }

}
