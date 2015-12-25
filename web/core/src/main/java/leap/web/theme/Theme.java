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
import leap.lang.Named;
import leap.web.view.ViewSource;

public interface Theme extends Named {
	
	/**
	 * Returns the {@link MessageSource} in this theme 
	 * 
	 * or returns <code>null</code> if there is no {@link MessageSource} in this theme.
	 */
	MessageSource getMessageSource();
	
	/**
	 * Returns the {@link AssetSource} in this theme
	 * 
	 * or returns <code>null</code> if there is no {@link AssetSource} in this theme.
	 */
	AssetSource getAssetSource();
	
	/**
	 * Returns the {@link ViewSource} in this theme
	 * 
	 * or returns <code>null</code> if there is no {@link ViewSource} in this theme.
	 */
	ViewSource getViewSource();
	
}