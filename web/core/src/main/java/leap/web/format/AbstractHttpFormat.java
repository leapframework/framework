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
package leap.web.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import leap.core.ioc.BeanNameAware;
import leap.core.validation.annotations.NotEmpty;
import leap.lang.Named;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;

public abstract class AbstractHttpFormat implements BeanNameAware,Named {
	
	protected @NotEmpty String 		   name;
	protected @NotEmpty List<MimeType> supportedMediaTypes;

	protected AbstractHttpFormat() {
		
	}
	
	protected AbstractHttpFormat(MimeType... supportedMediaTypes) {
		this.setSupportedMediaTypes(supportedMediaTypes);
	}

	@Override
    public String getName() {
	    return name;
    }

	@Override
    public void setBeanName(String name) {
		this.name = name;
    }

    /**
     * Returns the primary mime type of this format.
     */
    public MimeType getPrimaryMimeType() {
        return null == supportedMediaTypes || supportedMediaTypes.isEmpty() ? null : supportedMediaTypes.get(0);
    }
	
	public List<MimeType> getSupportedMediaTypes() {
		return supportedMediaTypes;
	}

	public void setSupportedMediaTypes(List<MimeType> supportedMediaTypes) {
		this.supportedMediaTypes = supportedMediaTypes;
	}
	
	public void setSupportedMediaTypes(MimeType... supportedMediaTypes){
		this.supportedMediaTypes = Arrays.asList(supportedMediaTypes);
	}
	
	public void addSupportedMediaTypes(MimeType mediaType){
		if(null == supportedMediaTypes){
			supportedMediaTypes = new ArrayList<>();
		}
		supportedMediaTypes.add(mediaType);
	}
	
	public void setSupportedMediaTypes(String[] supportedMediaTypes){
		this.supportedMediaTypes = new ArrayList<>();
		
		if(null != supportedMediaTypes){
			for(String type : supportedMediaTypes){
				this.supportedMediaTypes.add(MimeTypes.parse(type));
			}
		}
	}

    public boolean supports(MimeType mediaType) {
		if(mediaType.isWildcardType()){
			return true;
		}
		
		for(MimeType supportedMediaType : supportedMediaTypes){
			if(supportedMediaType.isCompatible(mediaType)){
				return true;
			}
		}
		
		return false;
    }
}
