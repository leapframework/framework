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

package leap.core;

import leap.agent.AgentPremain;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.lang.instrument.Instrumentation;

public class AppPremain implements AgentPremain {

    private static final Log log = LogFactory.get(AppPremain.class);

    @Override
    public void run(String args, Instrumentation inst) throws Exception {
        if(Strings.isEmpty(args)) {
            log.info("\nStart app context by javaagent!\n");
            AppContextInitializer.initStandalone();
        }
    }

}