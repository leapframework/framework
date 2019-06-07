/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot;

import org.springframework.boot.SpringBootVersion;

public class SpringBootUtils {

    private static final boolean is_spring_boot_1x;
    static {
        String v = SpringBootVersion.getVersion();
        if(v.startsWith("1.")) {
            is_spring_boot_1x = true;
        }else {
            is_spring_boot_1x = false;
        }
    }

    public static boolean is1x() {
        return is_spring_boot_1x;
    }

}