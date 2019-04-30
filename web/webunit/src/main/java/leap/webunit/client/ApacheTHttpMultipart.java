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
package leap.webunit.client;

import leap.lang.Args;
import leap.lang.http.MimeTypes;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public class ApacheTHttpMultipart extends THttpMultipartBase {
	
	private final MultipartEntityBuilder mp = MultipartEntityBuilder.create();
	

	ApacheTHttpMultipart(THttpRequest request) {
        super(request);
	    this.mp.setCharset(charset);
	}
	
	HttpEntity buildEntity() {
	    return mp.build();
	}

    @Override
    public THttpMultipart addFile(String name, String text, String filename, String contentType) {
        Args.notEmpty(filename, "filename");

        ContentType contentTypeObject = null;

        if(null == contentType) {
            contentType = MimeTypes.getMimeType(filename);
            if(null != contentType) {
                contentTypeObject = ContentType.create(contentType, charset);
            }
        }else{
            contentTypeObject = ContentType.create(contentType);
        }

        if(null == contentTypeObject) {
            contentTypeObject = ContentType.create("text/plain",charset.name());
        }

        byte[] data = null == text ? new byte[]{} : text.getBytes(charset);

        mp.addBinaryBody(name, data, contentTypeObject, filename);
        empty = false;
        return this;
    }

    @Override
    protected void addTextPart(String name, String text, String contentType) {
        mp.addTextBody(name, text, null == contentType ? ContentType.DEFAULT_TEXT : ContentType.create(contentType));
    }

    @Override
    protected void addBinaryPart(String name, byte[] b, String contentType, String filename) {
        mp.addBinaryBody(name, b, null == contentType ? ContentType.DEFAULT_BINARY : ContentType.create(contentType), filename);
    }
}