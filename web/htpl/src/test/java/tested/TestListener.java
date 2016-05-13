/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tested;

import leap.core.web.RequestBase;
import leap.htpl.*;
import leap.lang.Strings;
import leap.lang.intercepting.State;

public class TestListener implements HtplListener {

    @Override
    public void onTemplateCreated(HtplEngine engine, HtplTemplate template) throws Throwable {

        template.addInterceptor(new HtplTemplateInterceptor() {
            @Override
            public State preRenderTemplate(HtplTemplate template, HtplContext context, HtplWriter writer) throws Throwable {
                RequestBase request = context.getRequest();

                if(null != request) {
                    if(!Strings.isEmpty(request.getParameter("__view_name__"))){
                        writer.append("<!--view name:" + template.getName() + "-->");
                    }
                }

                return State.CONTINUE;
            }
        });

    }
}