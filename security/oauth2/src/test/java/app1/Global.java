/*
 * Copyright 2014 the original author or authors.
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
package app1;

import leap.core.annotation.Inject;
import leap.oauth2.rs.ResourceServerConfigurator;
import leap.web.App;
import leap.web.config.WebConfigurator;

public class Global extends App {
    
    protected @Inject ResourceServerConfigurator rc;

    @Override
    protected void configure(WebConfigurator c) {
        configure(rc.enable());
    }

    private void configure(ResourceServerConfigurator c) {
        c.setRemoteServerUrl("https://127.0.0.1:8443/server");
    }
}