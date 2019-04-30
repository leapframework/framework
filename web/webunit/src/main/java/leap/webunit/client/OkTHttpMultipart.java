/*
 * Copyright 2017 the original author or authors.
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
import leap.lang.http.ContentTypes;
import leap.lang.http.MimeTypes;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class OkTHttpMultipart extends THttpMultipartBase {

    private final MultipartBody.Builder mp = new MultipartBody.Builder();

    public OkTHttpMultipart(THttpRequest request) {
        super(request);
    }

    RequestBody buildRequestBody() {
        mp.setType(MediaType.parse(ContentTypes.MULTIPART_FORM_DATA));
        return mp.build();
    }

    @Override
    protected void addTextPart(String name, String text, String contentType) {
        mp.addPart(MultipartBody.Part.createFormData(name, text));
    }

    @Override
    protected void addBinaryPart(String name, byte[] b, String contentType, String filename) {
        if(null == contentType) {
            contentType = ContentTypes.APPLICATION_OCTET_STREAM;
        }

        MediaType mediaType = MediaType.parse(contentType);
        mp.addFormDataPart(name, filename, RequestBody.create(mediaType, b));
    }

    @Override
    public THttpMultipart addFile(String name, String text, String filename, String contentType) {
        Args.notEmpty(filename, "filename");

        if(null == contentType) {
            contentType = MimeTypes.getMimeType(filename);
            if(null == contentType) {
                contentType = "text/plain";
            }
        }

        MediaType mediaType = MediaType.parse(MimeTypes.parse(contentType).withCharset(charset.name()).toString());

        byte[] data = null == text ? new byte[]{} : text.getBytes(charset);

        mp.addFormDataPart(name, filename, RequestBody.create(mediaType, data));
        empty = false;
        return this;
    }
}
