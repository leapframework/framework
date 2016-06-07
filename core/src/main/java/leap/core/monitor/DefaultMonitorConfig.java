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

import leap.core.annotation.Configurable;
import leap.core.ioc.ConfigBean;

@Configurable(prefix = "app.monitor")
public class DefaultMonitorConfig implements MonitorConfig, ConfigBean {

    protected boolean enabled;
    protected boolean reportError;
    protected boolean reportArgs;
    protected boolean reportLineNumber;
    protected int     methodThreshold = DEFAULT_METHOD_THRESHOLD;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isReportError() {
        return reportError;
    }

    public void setReportError(boolean reportError) {
        this.reportError = reportError;
    }

    @Override
    public boolean isReportArgs() {
        return reportArgs;
    }

    public void setReportArgs(boolean reportArgs) {
        this.reportArgs = reportArgs;
    }

    @Override
    public boolean isReportLineNumber() {
        return reportLineNumber;
    }

    public void setReportLineNumber(boolean reportLineNumber) {
        this.reportLineNumber = reportLineNumber;
    }

    @Override
    public int getMethodThreshold() {
        return methodThreshold;
    }

    public void setMethodThreshold(int methodThreshold) {
        this.methodThreshold = methodThreshold;
    }

}