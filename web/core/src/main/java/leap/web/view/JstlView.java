/*
 * Copyright 2013 the original author or authors.
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
package leap.web.view;

import leap.core.i18n.MessageSource;
import leap.lang.resource.Resource;
import leap.web.App;
import leap.web.Request;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public class JstlView extends RequestDispatchView {

	public JstlView(App app, String path, Resource resource) {
	    super(app, path, resource);
    }

	@Override
    protected void exposeHelpers(Request request) throws Exception {
		super.exposeHelpers(request);
		
		Config.set(request.getServletRequest(), Config.FMT_LOCALE, request.getLocale());
		
		//TODO : timezone
		
		LocalizationContext jstlContext = new JstlViewLocalizationContext(request.getMessageSource(), request);
		Config.set(request.getServletRequest(), Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
    }
	
	private static class JstlViewLocalizationContext extends LocalizationContext {

		private final MessageSource messageSource;
		private final Request 		request;

		public JstlViewLocalizationContext(MessageSource messageSource, Request request) {
			this.messageSource = messageSource;
			this.request 	   = request;
		}

		@Override
		public ResourceBundle getResourceBundle() {
			HttpSession session = this.request.getServletRequest().getSession(false);
			if (session != null) {
				Object lcObject = Config.get(session, Config.FMT_LOCALIZATION_CONTEXT);
				if (lcObject instanceof LocalizationContext) {
					ResourceBundle lcBundle = ((LocalizationContext) lcObject).getResourceBundle();
					return new MessageSourceResourceBundle(this.messageSource, getLocale(), lcBundle);
				}
			}
			return new MessageSourceResourceBundle(this.messageSource, getLocale());
		}

		@Override
		public Locale getLocale() {
			HttpSession session = this.request.getServletRequest().getSession(false);
			if (session != null) {
				Object localeObject = Config.get(session, Config.FMT_LOCALE);
				if (localeObject instanceof Locale) {
					return (Locale) localeObject;
				}
			}
			return request.getLocale();
		}
	}
	
	private static class MessageSourceResourceBundle extends ResourceBundle {
		private final MessageSource messageSource;
		private final Locale		locale;
		
		public MessageSourceResourceBundle(MessageSource messageSource,Locale locale) {
			this.messageSource = messageSource;
			this.locale        = locale;
		}
		
		public MessageSourceResourceBundle(MessageSource source, Locale locale, ResourceBundle parent) {
			this(source, locale);
			setParent(parent);
		}

		@Override
		protected Object handleGetObject(String key) {
			return this.messageSource.getMessage(key, null, this.locale);
		}

		@Override
		public boolean containsKey(String key) {
			return null != this.messageSource.getMessage(key, null, this.locale);
		}

		@Override
        public Enumeration<String> getKeys() {
			throw new UnsupportedOperationException("MessageSourceResourceBundle does not support enumerating its keys");
        }
		
		@Override
		public Locale getLocale() {
			return this.locale;
		}
	}
}