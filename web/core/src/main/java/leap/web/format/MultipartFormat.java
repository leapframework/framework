/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.format;

import leap.lang.http.Headers;
import leap.lang.http.MimeTypes;
import leap.web.Request;
import leap.web.action.Action;
import leap.web.action.Argument;
import leap.web.action.MultipartArgumentResolver;
import leap.web.multipart.MultipartFile;
import leap.web.multipart.StandardMultipartFile;

import javax.servlet.http.Part;
import java.io.IOException;

/**
 * Created by kael on 2016/11/11.
 */
public class MultipartFormat extends AbstractHttpFormat implements RequestFormat  {
    private static final String FILENAME_KEY = "filename=";

    private MultipartArgumentResolver resolver;

    public MultipartFormat() {
        super(MimeTypes.MULTIPART_FORM_DATA_TYPE);
    }

    @Override
    public boolean supports(Action action) {
        return true;
    }

    @Override
    public boolean supportsRequestBody() {
        return true;
    }

    @Override
    public Object readRequestBody(Request request, Argument argument) throws IOException, IllegalStateException {
        if(!request.isMultipart()) {
            return null;
        }

        Part part = request.getPart(argument.getName());
        if(part == null){
            return null;
        }
        if(!argument.getType().equals(MultipartFile.class)) {
            return part;
        }else{
            String filename = extractFilename(part.getHeader(Headers.CONTENT_DISPOSITION));
            if (filename != null) {
                return new StandardMultipartFile(part, filename);
            }else{
                //not a file.
                return null;
            }
        }
    }
    private String extractFilename(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }
        // TODO: can only handle the typical case at the moment
        int startIndex = contentDisposition.indexOf(FILENAME_KEY);
        if (startIndex == -1) {
            return null;
        }
        String filename = contentDisposition.substring(startIndex + FILENAME_KEY.length());
        if (filename.startsWith("\"")) {
            int endIndex = filename.indexOf("\"", 1);
            if (endIndex != -1) {
                return filename.substring(1, endIndex);
            }
        }
        else {
            int endIndex = filename.indexOf(";");
            if (endIndex != -1) {
                return filename.substring(0, endIndex);
            }
        }
        return filename;
    }
}
