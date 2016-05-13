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
package leap.htpl.resolver;

import leap.htpl.HtplEngine;
import leap.htpl.HtplResource;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplTemplateResolver;
import leap.lang.Locales;

import java.util.Locale;

public abstract class AbstractHtplResolver implements HtplTemplateResolver {

	protected HtplEngine engine;
	protected String     prefix;
	protected String     suffix;
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public void setEngine(HtplEngine engine) {
		this.engine = engine;
	}

	@Override
    public final HtplTemplate resolveTemplate(String templateName, Locale locale) {
		String[] locations = getResourceLocations(templateName, locale);
		
		HtplResource r;
		for(int i=0;i<locations.length;i++){
			if((r = tryResolveResource(locations[i])) != null){
				return engine.createTemplate(r, templateName);
			}
		}
		
	    return null;
    }
	
	protected abstract HtplResource tryResolveResource(String location);
	
	protected String[] getResourceLocations(String templateName,Locale locale){
		String[] localeTemplateNames = Locales.getLocalePaths(locale, templateName);
		String[] resoruceLocations   = new String[localeTemplateNames.length];
		
		for(int i=0;i<resoruceLocations.length;i++){
			String location;
			
			if(null != prefix && null != suffix){
				location = prefix + localeTemplateNames[i] + suffix;
			}else if(null != prefix){
				location = prefix + localeTemplateNames[i];
			}else{
				location = localeTemplateNames[i] + suffix;
			}
			
			resoruceLocations[i] = location;
		}
		return resoruceLocations;
	}
}