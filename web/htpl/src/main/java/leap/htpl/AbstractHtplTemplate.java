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
package leap.htpl;

import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractHtplTemplate implements HtplTemplate {

	private static final Log log = LogFactory.get(AbstractHtplTemplate.class);

	protected final List<HtplTemplateListener>    listeners    = new CopyOnWriteArrayList<>();
    protected final List<HtplTemplateInterceptor> interceptors = new CopyOnWriteArrayList<>();

    protected String name;

    public AbstractHtplTemplate() {

    }

    public AbstractHtplTemplate(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addListener(HtplTemplateListener listener) {
		listeners.add(listener);
    }

	@Override
    public boolean removeListener(HtplTemplateListener listener) {
	    return listeners.remove(listener);
    }

	@Override
    public boolean containsListener(HtplTemplateListener listener) {
	    return listeners.contains(listener);
    }

    @Override
    public void addInterceptor(HtplTemplateInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    @Override
    public boolean containsInterceptor(HtplTemplateInterceptor interceptor) {
        return interceptors.contains(interceptor);
    }

    @Override
    public boolean removeInterceptor(HtplTemplateInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    protected void notifyTemplateReloaded() {
		if(listeners.isEmpty()){
			return;
		}
		
		for(HtplTemplateListener l : listeners){
			try {
	            l.onTemplateReloaded(this);
            } catch (Throwable e) {
            	log.warn("Error notifying 'onTemplateReloaded' on listener '{}', {}",l,e.getMessage(),e);
            }
		}
	}

    protected boolean preRenderTemplate(HtplContext context, HtplWriter writer) throws Throwable  {
        for(HtplTemplateInterceptor interceptor : interceptors) {
            if(State.isIntercepted(interceptor.preRenderTemplate(this, context, writer))) {
                return false;
            }
        }
        return true;
    }

    protected void postRenderTemplate(HtplContext context, HtplWriter writer) throws Throwable {
        for(HtplTemplateInterceptor interceptor : interceptors) {
            interceptor.postRenderTemplate(this, context, writer);
        }
    }

}
