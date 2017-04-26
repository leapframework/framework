/*
 * Copyright 2015 the original author or authors.
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
package leap.htpl;

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;

@Configurable(prefix="htpl.")
public class DefaultHtplConfig implements HtplConfig,PostCreateBean {

    protected @Inject AppConfig appConfig;

	protected String  prefix;
    protected Boolean reloadEnabled;
    protected int     reloadInterval = HtplConstants.DEFAULT_RELOAD_INTERVAL;
	
	@Override
	public String getPrefix() {
		return prefix;
	}

	@ConfigProperty
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

    @Override
    public boolean isReloadEnabled() {
        return reloadEnabled;
    }

    @ConfigProperty
    public void setReloadEnabled(boolean reloadEnabled) {
        this.reloadEnabled = reloadEnabled;
    }

    @Override
    public int getReloadInterval() {
        return reloadInterval;
    }

    @ConfigProperty
    public void setReloadInterval(int reloadInterval) {
        this.reloadInterval = reloadInterval;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        if(null == this.reloadEnabled) {
            this.reloadEnabled = appConfig.isReloadEnabled();
        }
    }
}