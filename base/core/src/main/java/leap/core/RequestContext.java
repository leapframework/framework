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
package leap.core;

import java.util.Locale;

import leap.core.i18n.MessageSource;
import leap.core.security.Authentication;
import leap.lang.Locales;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.exception.ObjectNotFoundException;

public abstract class RequestContext implements AttributeAccessor {

	private   static RequestContext              standalone  = null;
	protected static ThreadLocal<RequestContext> threadlocal = new InheritableThreadLocal<RequestContext>();

	public static RequestContext current() {
		RequestContext c = tryGetCurrent();
		if(null == c) {
			throw new IllegalStateException("Current request context must be initialized");
		}
		return c;
	}
	
	public static RequestContext tryGetCurrent() {
		if(null != standalone){
			return standalone;
		}
		
		RequestContext current = threadlocal.get();
		
		if(null == current){
			if(null != standalone){
				return standalone;
			}
		}
		
		return current;
	}

    /**
     * Returns current request's locale or use {@link Locales#DEFAULT_LOCALE} if no current locale.
     */
    public static Locale locale() {
        RequestContext rc = tryGetCurrent();
        if(null != rc && null != rc.getLocale()) {
            return rc.getLocale();
        }else{
            return Locales.DEFAULT_LOCALE;
        }
    }
	
	protected static void setStandalone(RequestContext standalone){
		RequestContext.standalone = standalone;
	}
	
	public static void setCurrent(RequestContext current){
		threadlocal.set(current);
	}
	
	public static void removeCurrent(){
		threadlocal.remove();
	}
	
	/**
	 * Returns a formatted {@link String} of the given key and current locale.
	 * 
	 * @throws ObjectNotFoundException if the given key not exists.
	 * 
	 * @see MessageSource#getMessage(String, Object...)
	 */
	public static String getMessage(String key,Object... args) throws ObjectNotFoundException {
		RequestContext current = current();
		return current.getMessageSource().getMessage(current.getLocale(),key, args);
	}
	
	/**
	 * Returns a formatted {@link String} of the given key for current {@link Locale}.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given key not exists for current {@link Locale}.
	 * 
	 * @see MessageSource#tryGetMessage(Locale, String, Object...)
	 */
	public static String tryGetMessage(String key,Object... args) {
		RequestContext current = current();
		return current.getMessageSource().tryGetMessage(current.getLocale(),key, args);
	}
	
	protected Boolean debug;

	/**
	 * Returns new back ground context.
	 */
	public RequestContext newBackgroundContext() {
		return null;
	}
	
	/**
	 * Returns current {@link AppContext}.
	 */
	public abstract AppContext getAppContext();
	
	/**
	 * Returns current {@link Session} associated with this request context or, 
	 * if there is no current session, creates one.
	 */
	public abstract Session getSession();
	
	/**
	 * Returns the current {@link Session} associated with this
     * request context or, if there is no current session context and <code>create</code> is
     * true, returns a new session.
     *
     * <p>If <code>create</code> is <code>false</code> and the request has no
     * valid {@link Session}, this method returns <code>null</code>.
	 */
	public abstract Session getSession(boolean create);

	/**
	 * Returns current authentication or null.
	 */
	public abstract Authentication getAuthentication();

	/**
	 * Set current authentication.
	 */
	public abstract void setAuthentication(Authentication authentication);

	/**
	 * Returns current {@link Locale} in this request context.
	 */
	public abstract Locale getLocale();
	
	/**
	 * Sets current {@link Locale} in this request context.
	 */
	public abstract void setLocale(Locale locale);
	
	/**
	 * Returns current {@link MessageSource}.
	 */
	public abstract MessageSource getMessageSource();
	
	/**
	 * Sets current {@link MessageSource}.
	 */
	public abstract void setMessageSource(MessageSource messageSource);	
	
	/**
	 * Returns <code>true</code> if current request is in debug mode.
	 * 
	 * <p>
	 * Debug mode is useful to diagnose application's problem. 
	 * 
	 * <p>
	 * The default value is <code>true</code> if current application's profile is 'development' in {@link AppConfig#getProfile()}.
	 */
	public abstract boolean isDebug();
	
	/**
	 * Sets the debug mode in current request.
	 */
	public abstract void setDebug(boolean debug);
}