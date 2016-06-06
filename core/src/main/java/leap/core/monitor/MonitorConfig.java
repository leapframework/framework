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

public interface MonitorConfig {

    int DEFAULT_METHOD_THRESHOLD = 50;

    /**
     * Returns true if monitoring is enabled.
     */
    boolean isEnabled();

    /**
     * Returns true if report the error.
     */
    boolean isReportError();

    /**
     * Returns true if report the method arguments.
     */
    boolean isReportArgs();

    /**
     * Returns the threshold of milli-seconds for reporting method execution times.
     */
    int getMethodThreshold();
}
