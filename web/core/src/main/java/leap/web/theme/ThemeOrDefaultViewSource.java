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

import java.util.Locale;

import leap.web.view.View;
import leap.web.view.ViewSource;

public class ThemeOrDefaultViewSource implements ViewSource {
	
	private final ViewSource themeViewSource;
	private final ViewSource defaultViewSource;
	
	public ThemeOrDefaultViewSource(ViewSource themeViewSource,ViewSource defaultViewSource) {
		this.themeViewSource   = themeViewSource;
		this.defaultViewSource = defaultViewSource;
	}

	@Override
    public View getView(String viewName, Locale locale) {
		View view = themeViewSource.getView(viewName, locale);
	    return null == view ? defaultViewSource.getView(viewName, locale) : view;
    }

}