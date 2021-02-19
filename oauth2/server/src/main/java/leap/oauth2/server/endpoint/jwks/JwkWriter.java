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

import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.lang.naming.NamingStyle;
import leap.web.Request;
import leap.web.Response;
import leap.web.json.JsonConfig;
import leap.web.json.Jsonp;
import java.io.IOException;
import java.util.Collection;

/**
 * @author kael.
 */
public class JwkWriter {
    protected Request    request;
    protected Response   response;
    protected JsonConfig jc;

    private JwkWriter(Request request, Response response, JsonConfig jc) {
        this.request = request;
        this.response = response;
        this.jc = jc;
    }

    public static JwkWriter create(Request request) {
        JsonConfig jc = request.getAppContext().getBeanFactory().getBean(JsonConfig.class);
        
        return new JwkWriter(request, request.response(), jc);
    }

    public void write(JwksToken token) throws IOException {
        Jsonp.write(request, response, jc, writer -> {
            JsonWriter jsonWriter = JSON.writer(writer).setIgnoreNull(true).create();
            jsonWriter.startObject()
                    .property("keys", jw -> {
                        jw.startArray();
                        for(int i = 0; i < token.getKeys().size(); i++){
                            if (i != 0){
                                jw.separator();
                            }
                            writeToken(token.getKeys().get(i),jw);
                        }
                        jw.endArray();
                    })
                    .endObject();
        });
    }

    public void write(JwkToken token) throws IOException {
        Jsonp.write(request, response, jc, writer -> writeToken(token, JSON.writer(writer).setIgnoreNull(true).create()));
    }

    protected void writeToken(JwkToken token, JsonWriter writer) {
        writer.startObject()
                .property("kty", token.getKty())
                .property("value", token.getValue())
                .propertyIgnorable("use", token.getUse())
                .propertyIgnorable("key_ops", token.getKeyOps())
                .propertyIgnorable("alg", token.getAlg())
                .propertyIgnorable("kid", token.getKid())
                .propertyIgnorable("x5u", token.getX5u())
                .propertyIgnorable("x5c", token.getX5c())
                .propertyIgnorable("x5t", token.getX5t())
                .propertyIgnorable("n", token.getN())
                .propertyIgnorable("e", token.getE())
                .endObject();
    }
    private class WrapperJsonConfig implements JsonConfig{
        
        private final JsonConfig jc;

        public WrapperJsonConfig(JsonConfig jc) {
            this.jc = jc;
        }

        @Override
        public boolean isDefaultSerializationKeyQuoted() {
            return jc.isDefaultSerializationKeyQuoted();
        }

        @Override
        public boolean isDefaultSerializationIgnoreNull() {
            return jc.isDefaultSerializationIgnoreNull();
        }

        @Override
        public boolean isDefaultSerializationIgnoreEmpty() {
            return jc.isDefaultSerializationIgnoreEmpty();
        }

        @Override
        public boolean isJsonpEnabled() {
            return jc.isJsonpEnabled();
        }

        @Override
        public boolean isJsonpResponseHeaders() {
            return jc.isJsonpResponseHeaders();
        }

        @Override
        public boolean isHtmlEscape() {
            return jc.isHtmlEscape();
        }

        @Override
        public Collection<String> getJsonpAllowResponseHeaders() {
            return jc.getJsonpAllowResponseHeaders();
        }

        @Override
        public String getJsonpParameter() {
            return jc.getJsonpParameter();
        }

        @Override
        public NamingStyle getDefaultNamingStyle() {
            return jc.getDefaultNamingStyle();
        }

        @Override
        public String getDefaultDateFormat() {
            return jc.getDefaultDateFormat();
        }
    }
}
