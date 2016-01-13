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
package leap.htpl;

import java.util.Locale;

import leap.core.RequestContext;
import leap.core.i18n.MessageSource;
import leap.core.web.RequestBase;
import leap.web.assets.AssetSource;

public class DefaultHtplContext extends AbstractHtplContext{

	protected String	    	 contextPath;
	protected RequestBase   	 request;
	protected Locale	    	 locale;
	protected Boolean			 debug;
	protected MessageSource		 messageSource;
	protected AssetSource		 assetSource;
	
	public DefaultHtplContext(HtplEngine engine) {
	    super(engine);
	    this.assetSource = engine.getAssetSource();
    }

	@Override
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public RequestBase getRequest() {
		return request;
	}

	public void setRequest(RequestBase request) {
		this.request = request;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	@Override
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public AssetSource getAssetSource() {
		return assetSource;
	}

	public void setAssetSource(AssetSource assetSource) {
		this.assetSource = assetSource;
	}
	
	@Override
    public boolean isDebug() {
		if(null == debug){
			RequestContext rc = RequestContext.tryGetCurrent();
			debug = null != rc ? rc.isDebug() : false;
		}
	    return debug;
    }

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
