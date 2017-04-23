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

import leap.lang.Args;
import leap.lang.http.HTTP;
import leap.lang.meta.MType;

import java.util.Map;

public class MApiResponse extends MApiObjectWithDesc {

    protected final String       name;
    protected final Integer      status;
    protected final MType        type;
    protected final boolean      file;
    protected final MApiHeader[] headers;

	public MApiResponse(String name, MType type) {
		this(name, null, null, HTTP.SC_OK, type, false, null, null);
	}

    public MApiResponse(String name, MType type, boolean file) {
        this(name, null, null, HTTP.SC_OK, type, file, null, null);
    }

    public MApiResponse(String name, String summary, String description, Integer status, MType type,
                        boolean file, MApiHeader[] headers, Map<String, Object> attrs) {
	    super(summary, description, attrs);

        Args.notEmpty(name, "name");

        this.name   = name;
	    this.status = status;
	    this.type   = type;
        this.file   = file;
        this.headers = null == headers ? new MApiHeader[0] : headers;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
	 * Returns the http status of this response.
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * The response value's type, can be <code>null</code> if no response value.
	 */
	public MType getType() {
		return type;
	}

    /**
     * Returns true if the response type is file.
     */
    public boolean isFile() {
        return file;
    }

    /**
     * Returns the response headers.
     */
    public MApiHeader[] getHeaders() {
        return headers;
    }
}