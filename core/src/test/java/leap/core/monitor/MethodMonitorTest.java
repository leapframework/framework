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

package leap.core.monitor;

import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.lang.Try;
import org.junit.Test;
import tested.beans.TMonitorBean;

public class MethodMonitorTest extends AppTestBase {

    private @Inject TMonitorBean bean;

    @Test
    public void testSimple() {
        for(int i=0;i<10;i++) {
            Try.catchAll(() -> bean.test(1, "a"));
        }
    }

}
