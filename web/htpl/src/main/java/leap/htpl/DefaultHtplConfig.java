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

import leap.core.annotation.Configurable;
import leap.core.validation.annotations.NotEmpty;

@Configurable(prefix="htpl.")
public class DefaultHtplConfig implements HtplConfig {

	protected @NotEmpty String prefix;
	protected boolean reloadEnable = true;
	
	
	@Override
	public String getPrefix() {
		return prefix;
	}

	@Configurable.Property
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	@Override
	public boolean isReloadEnable() {
		return reloadEnable;
	}
	@Configurable.Property
	public void setReloadEnable(boolean reloadEnable) {
		this.reloadEnable = reloadEnable;
	}
}