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

import com.sun.tools.attach.VirtualMachine;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.lang.management.ManagementFactory;
import java.net.URL;

public class SunJdkAgentLoader {
    private static final Log log = LogFactory.get(SunJdkAgentLoader.class);

    private static String jarUrl;

    static {
        String classFile = AgentLoader.class.getName().replace('.','/') + ".class";
        URL url = AgentLoader.class.getClassLoader().getResource(classFile);

        String file = url.getFile();
        int index = file.indexOf(".jar!/");
        if(index > 0) {
            jarUrl = file.substring(5,index + 4);
        }else{
            log.error("Agent jar file not found, cannot redefine classes!!!");
        }
    }

    static boolean load() {
        if(null == jarUrl) {
            return false;
        }

        try{
            VirtualMachine vm = VirtualMachine.attach(pid());

            vm.loadAgent(jarUrl);

            vm.detach();

            return true;
        }catch(Exception e) {
            log.error("Error loading agent : {}" + e.getMessage(), e);
            return false;
        }
    }

    private static String pid() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String processID = processName.substring(0, processName.indexOf('@'));
        return processID;
    }

}
