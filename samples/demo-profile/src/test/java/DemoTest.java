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

import leap.junit.TestBase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class DemoTest extends TestBase {

    private static Set<String> ignores = new HashSet<>();

    static {
        ignores.add("os.");
        ignores.add("java.");
        ignores.add("user.");
        ignores.add("sun.");
        ignores.add("org.apache.jasper.");
        ignores.add("file.");
        ignores.add("surefire.");
        ignores.add("path.");
        ignores.add("awt.");
        ignores.add("line.");
        ignores.add("gopherProxySet");
    }

    @Test
    public void testPrintSystemProperties() {
        System.out.println();
        System.out.println();
        System.out.println("===============================================================================");
        System.out.println();
        System.out.println();

        Properties props = System.getProperties();
        for(Object key : props.keySet()) {
            String name = key.toString();

            boolean ignored = false;
            for(String ignore : ignores) {
                if(name.startsWith(ignore)) {
                    ignored = true;
                    break;
                }
            }

            if(!ignored) {
                System.out.println((String)key + " : " + props.getProperty((String)key));
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("===============================================================================");
        System.out.println();
        System.out.println();

    }
}
