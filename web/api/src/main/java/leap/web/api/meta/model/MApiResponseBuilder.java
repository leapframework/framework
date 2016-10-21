/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta.model;

import leap.lang.http.HTTP;
import leap.lang.meta.MType;

public class MApiResponseBuilder extends MApiObjectWithDescBuilder<MApiResponse> {
	
	public static MApiResponseBuilder ok() {
		MApiResponseBuilder r = new MApiResponseBuilder();
		
		r.setStatus(HTTP.SC_OK);
		r.setSummary("Success");
		
		return r;
	}

    public static MApiResponseBuilder success(int status) {
        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(status);
        r.setSummary("Success");

        return r;
    }

    public MApiResponseBuilder() {

    }

    public MApiResponseBuilder(MApiResponse r) {
        this.name = r.getName();
        this.status = r.getStatus();
        this.type = r.getType();
        this.file = r.isFile();
        this.setSummary(r.getSummary());
        this.setDescription(r.getDescription());
    }

    protected String  name;
	protected Integer status;
	protected MType   type;
    protected boolean file;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public MType getType() {
		return type;
	}

	public void setType(MType type) {
		this.type = type;
	}

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    @Override
    public MApiResponse build() {
        if(name == null && status == null) {
            throw new IllegalStateException("'name' or 'status' must not be specified!");
        }

        if(null == name) {
            name = String.valueOf(status);
        }

	    return new MApiResponse(name, summary, description, status, type, file, attrs);
    }
	
}