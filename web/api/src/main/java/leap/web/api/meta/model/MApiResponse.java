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

import java.util.Map;

public class MApiResponse extends MApiObjectWithDesc {
	
	public static final MApiResponse VOID = new MApiResponse(null);

    protected final int     status;
    protected final MType   type;
    protected final boolean file;

	public MApiResponse(MType type) {
		this(null, null, HTTP.SC_OK, type, false, null);
	}

    public MApiResponse(MType type, boolean file) {
        this(null, null, HTTP.SC_OK, type, file, null);
    }

    public MApiResponse(String summary, String description, int status, MType type, boolean file, Map<String, Object> attrs) {
	    super(summary, description, attrs);
	    
	    this.status = status;
	    this.type   = type;
        this.file   = file;
    }

	/**
	 * Returns the http status of this response.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * The response value's type, can be <code>null</code> if no response value.
	 */
	public MType getType() {
		return type;
	}

}