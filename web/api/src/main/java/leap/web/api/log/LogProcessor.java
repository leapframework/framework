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

package leap.web.api.log;

import leap.core.annotation.Inject;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadataProcessor;

/**
 * Created by kael on 2016/10/12.
 */
public class LogProcessor  implements ApiConfigProcessor,ApiMetadataProcessor {

    public static final String PATH = "/log";

    protected @Inject App app;
    protected @Inject Apis apis;

    @Override
    public void preProcess(ApiConfigurator c) {
        /*
        app.routes().create().get(getLogBasePath(c.config()), (req, resp) -> {
            queryLog(c.config(), req, resp);
        }).enableCors().apply();
        */
    }

    protected void queryLog(ApiConfig c, Request req, Response resp){

    }

    protected String getLogBasePath(ApiConfig config){
        return config.getBasePath() + PATH;
    }
}
