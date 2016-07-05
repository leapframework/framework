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

import java.nio.charset.Charset;

import leap.lang.Args;
import leap.lang.http.MimeTypes;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public class THttpMultipartImpl implements THttpMultipart {
	
    private final THttpRequest           request;
    private final Charset                charset;
	private final MultipartEntityBuilder mp = MultipartEntityBuilder.create();
	
	private boolean empty;
	
	THttpMultipartImpl(THttpRequest request) {
	    this.request = request;
	    this.charset = request.getCharset();
	    this.mp.setCharset(charset);
	}
	
	@Override
    public THttpRequest request() {
        return request;
    }

    @Override
    public THttpResponse send() {
        return request.send();
    }

    boolean isEmpty() {
	    return empty;
	}
	
	HttpEntity buildEntity() {
	    return mp.build();
	}
	
    @Override
    public THttpMultipart addBytes(String name, byte[] data) {
		mp.addBinaryBody(name, data);
		empty = false;
	    return this;
    }

	@Override
    public THttpMultipart addBytes(String name, byte[] data, String contentType) {
		mp.addBinaryBody(name, data, null == contentType ? null : ContentType.create(contentType), null);
		empty = false;
	    return this;
    }
	
	@Override
    public THttpMultipart addFile(String name, byte[] data, String filename) {
	    return addFile(name, data, filename, null);
    }
	
	@Override
    public THttpMultipart addFile(String name, byte[] data, String filename, String contentType) {
		Args.notEmpty(filename, "filename");
		
		if(null == contentType) {
			contentType = MimeTypes.getMimeType(filename);
		}
		
		mp.addBinaryBody(name, data, null == contentType ? null : ContentType.create(contentType), filename);
		empty = false;
		return this;
    }
	
	@Override
    public THttpMultipart addText(String name, String text) {
		mp.addTextBody(name, text);
		empty = false;
	    return this;
    }

	@Override
    public THttpMultipart addText(String name, String text, String contentType) {
		mp.addTextBody(name, text, null == contentType ? null : ContentType.create(contentType));
		empty = false;
		return this;
    }
	
	@Override
    public THttpMultipart addFile(String name, String text, String filename) {
	    return addFile(name, text, filename, null);
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
}