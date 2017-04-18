/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.convert;

import java.lang.reflect.Type;
import java.sql.Blob;

import leap.lang.Charsets;
import leap.lang.Out;
import leap.lang.io.IO;
import leap.lang.jdbc.SimpleBlob;

public class BlobConverter extends AbstractConverter<Blob> {

	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
		if(value instanceof byte[]){
			out.set(new SimpleBlob((byte[])value));
			return true;
		}
		return false;
    }

	@Override
    public boolean convertTo(Blob value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
		//TODO : close inputstream ?
	    byte[] data = IO.readByteArray(value.getBinaryStream());
	    
	    out.set(Converts.convert(data, targetType, genericType));
	    return true;
    }

	@Override
    public String convertToString(Blob value) throws Throwable {
	    return IO.readString(value.getBinaryStream(), Charsets.UTF_8);
    }
}
