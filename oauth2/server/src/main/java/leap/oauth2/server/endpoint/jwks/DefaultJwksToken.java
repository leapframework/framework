/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.oauth2.server.endpoint.jwks;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kael.
 */
public class DefaultJwksToken implements JwksToken, PostCreateBean {

    protected @Inject @M List<JwkToken> keys;

    @Override
    public List<JwkToken> getKeys() {
        return keys;
    }

    public void setKeys(List<JwkToken> keys) {
        this.keys = keys;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        keys.stream().collect(Collectors.groupingBy(JwkToken::getKid))
                .values().stream().filter(jwkTokens -> jwkTokens.size() > 1)
                .findAny().ifPresent(jwkTokens -> {
            StringBuilder sb = new StringBuilder("duplicate kid in jwk tokens");
            sb.append("\n");
            JsonWriter jw = JSON.createWriter(sb);
            jw.startArray();
            sb.append("\n");
            for(int i = 0; i < jwkTokens.size(); i++) {
                if (i != 0) {
                    jw.separator();
                    sb.append("\n");
                }
                JwkToken token = jwkTokens.get(i);
                jw.startObject()
                        .property("kid", token.getKid())
                        .property("value", token.getValue())
                        .endObject();
            }
            sb.append("\n");
            jw.endArray();
            throw new IllegalStateException(sb.toString());
        });
    }
}
