/*
 * Copyright 2016 the original author or authors.
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
package app;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.R;
import leap.web.App;
import leap.web.AppBootable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Configurable
public class DemoBootstrap implements AppBootable {

    protected @ConfigProperty @R String configValue;

    @Override
    public void postBootApp(App app, ServletContext sc) throws ServletException {

    }

}