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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public abstract class AbstractHtplTemplate implements HtplTemplate {

	private static final Log log = LogFactory.get(AbstractHtplTemplate.class);
	
	protected final Set<HtplTemplateListener> listeners = new CopyOnWriteArraySet<HtplTemplateListener>();
	
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
}
