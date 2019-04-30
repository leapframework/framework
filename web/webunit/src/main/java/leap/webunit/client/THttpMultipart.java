/*
 * Copyright 2015 the original author or authors.
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

public interface THttpMultipart {
    
    /**
     * Returns the {@link THttpRequest}.
     */
    THttpRequest request();
    
    /**
     * Adds a binary part.
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addBytes(String name,byte[] data);
    
    /**
     * Adds a binary part.
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addBytes(String name,byte[] data, String contentType);
    
    /**
     * Adds a plain text part with text/plain content type.
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addText(String name,String text);
    
    /**
     * Adds a text part with the given content type..
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addText(String name,String text, String contentType);
    
    /**
     * Adds a file part.
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addFile(String name, byte[] data, String filename, String contentType);
    
    /**
     * Adds a file part.
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addFile(String name, byte[] data, String filename);
    
    /**
     * Adds a file part.
     * 
     * @param name the part name (form's input name).
     */
    THttpMultipart addFile(String name, String text, String filename);
    
    /**
     * Adds a file part.
     * 
     * @param name the part name
     * @param text the content of file.
     */
    THttpMultipart addFile(String name, String text, String filename, String contentType);

    /**
     * Sends the multipart request.
     */
    default THttpResponse send() {
        return request().send();
    }

    /**
     * Sends the multipart request.
     */
    default THttpResponse put() {
        return request().put();
    }

    /**
     * Sends the multipart request.
     */
    default THttpResponse post() {
        return request().post();
    }
}