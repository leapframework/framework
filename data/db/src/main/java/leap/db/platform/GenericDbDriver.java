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
package leap.db.platform;

import java.util.Map;

import leap.core.i18n.I18N;
import leap.core.i18n.Localizable;
import leap.core.i18n.MessageSource;
import leap.core.meta.MSimpleParameter;
import leap.db.DbDriver;
import leap.lang.Classes;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.meta.AbstractMNamedWithDesc;
import leap.lang.st.stpl.StplTemplate;

public class GenericDbDriver extends AbstractMNamedWithDesc implements DbDriver, Localizable {
	
	protected Type				 type = Type.SERVER;
	protected String			 fileExtension;
	protected String 			 driverClassName;
	protected String			 urlTemplate;
	protected MSimpleParameter[] urlParameters;
	
	private Boolean 	 driverClassAvailable;
	private StplTemplate parsedUrlTemplate;
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	@Override
    public String getDriverClassName() {
	    return driverClassName;
    }
	
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
	
	public MSimpleParameter[] getUrlParameters() {
		return urlParameters;
	}

	public void setUrlParameters(MSimpleParameter[] urlParameters) {
		this.urlParameters = urlParameters;
	}
	
	@Override
    public MSimpleParameter getUrlParameter(String name) {
		if(urlParameters != null){
			for(int i=0;i<urlParameters.length;i++){
				MSimpleParameter p = urlParameters[i];
				if(p.getName().equals(name)){
					return p;
				}
			}
		}
		throw new ObjectNotFoundException("Parameter '" + name + "' not found in this driver");
	}
	
	@Override
    public String getUsername(Map<String, Object> params) {
	    return null == params ? null : (String)params.get(USERNAME_PARAMETER);
    }

	@Override
    public String getPassword(Map<String, Object> params) {
		return null == params ? null : (String)params.get(PASSWROD_PARAMETER);
    }

	public String getUrlTemplate() {
		return urlTemplate;
	}

	public void setUrlTemplate(String urlTemplate) {
		this.urlTemplate = urlTemplate;
		this.parsedUrlTemplate = StplTemplate.parse(urlTemplate);
	}

	@Override
    public String getUrl(Map<String, Object> params) {
	    return parsedUrlTemplate.render(params);
    }

	@Override
    public boolean isAvailable() {
		if(null == driverClassAvailable){
			driverClassAvailable = Classes.isPresent(driverClassName);
		}
		return driverClassAvailable;
	}

	@Override
    public boolean isFile() {
	    return type == Type.FILE;
    }

	@Override
    public boolean isMemory() {
	    return type == Type.MEM;
    }

	@Override
    public void localize(MessageSource ms, String localizeKey) {
		if(null != urlParameters){
			String keyPrefix = "db.drivers.params";
			for(int i=0;i<urlParameters.length;i++){
				MSimpleParameter p = urlParameters[i];
				MSimpleParameter.Builder lp = new MSimpleParameter.Builder(p);
				
				I18N.localize(ms, lp, keyPrefix + "." + p.getName());
				
				urlParameters[i] = lp.build();
			}
		}
    }
}