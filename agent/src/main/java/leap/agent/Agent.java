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

package leap.agent;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class Agent {

    private static final Log log = LogFactory.get(Agent.class);

    private static Map<Class<?>,? extends InstrumentClass> redefineClasses;
    private static boolean                                 redefined;

    public static boolean redefine(Map<Class<?>,? extends InstrumentClass> classes) {
        redefined = false;
        redefineClasses = classes;

        if(!AgentLoader.load()){
            return false;
        }

        return redefined;
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        if(null != redefineClasses) {

            log.info("Redefine {} classes!!!",redefineClasses.size());

            for(Map.Entry<Class<?>,? extends InstrumentClass> entry : redefineClasses.entrySet()) {
                Class<?> c = entry.getKey();
                InstrumentClass ic = entry.getValue();

                log.info("  do redefine '{}'", c.getName());

                inst.redefineClasses(new ClassDefinition(c, ic.getClassData()));
            }

            redefined = true;
        }
    }

}