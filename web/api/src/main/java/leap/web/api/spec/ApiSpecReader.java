/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.web.api.spec;

import leap.lang.Exceptions;
import leap.web.api.meta.ApiMetadataBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public interface ApiSpecReader {

    /**
     * Returns the api spec to {@link ApiMetadataBuilder}.
     */
    default ApiMetadataBuilder read(String spec)  {
       try(Reader reader = new StringReader(spec)){
           return read(reader);
       }catch(IOException e) {
           Exceptions.wrapAndThrow(e);
           return null;
       }
    }

    /**
     * Reads the api spec content to {@link ApiMetadataBuilder}.
     */
    ApiMetadataBuilder read(Reader reader) throws IOException;

}
