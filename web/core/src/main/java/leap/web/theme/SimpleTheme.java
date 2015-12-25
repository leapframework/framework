/*
 * Copyright 2014 the original author or authors.
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
package leap.web.theme;

import leap.core.i18n.MessageSource;
import leap.core.web.assets.AssetSource;
import leap.web.view.ViewSource;

public class SimpleTheme implements Theme {
	
	protected final String        name;
	protected final MessageSource messageSource;
	protected final AssetSource   assetSource;
	protected final ViewSource    viewSource;
	
	public SimpleTheme(String name, MessageSource messageSource, AssetSource assetSource, ViewSource viewSource) {
		super();
		this.name = name;
		this.messageSource = messageSource;
		this.assetSource = assetSource;
		this.viewSource = viewSource;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Override
	public AssetSource getAssetSource() {
		return assetSource;
	}

	@Override
	public ViewSource getViewSource() {
		return viewSource;
	}
	
	public static final class Builder {
		
		private String        name;
		private MessageSource messageSource;
		private AssetSource   assetSource;
		private ViewSource    viewSource;
		
		public String getName() {
			return name;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public MessageSource getMessageSource() {
			return messageSource;
		}

		public Builder setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
			return this;
		}

		public AssetSource getAssetSource() {
			return assetSource;
		}

		public Builder setAssetSource(AssetSource assetSource) {
			this.assetSource = assetSource;
			return this;
		}

		public ViewSource getViewSource() {
			return viewSource;
		}

		public Builder setViewSource(ViewSource viewSource) {
			this.viewSource = viewSource;
			return this;
		}

		public SimpleTheme build() {
			return new SimpleTheme(name, messageSource, assetSource, viewSource);
		}
	}
}