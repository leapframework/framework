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
package leap.web.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.Part;

import leap.lang.io.IO;

/**
 * Wrapping a Servlet 3.0 Part object.
 */
public class StandardMultipartFile implements MultipartFile {
	
	private final Part   part;
	private final String filename;

	public StandardMultipartFile(Part part, String filename) {
		this.part = part;
		this.filename = filename;
	}

	@Override
	public String getName() {
		return this.part.getName();
	}

	@Override
	public String getOriginalFilename() {
		return this.filename;
	}

	@Override
	public String getContentType() {
		return this.part.getContentType();
	}

	@Override
	public boolean isEmpty() {
		return (this.part.getSize() == 0);
	}

	@Override
	public long getSize() {
		return this.part.getSize();
	}
	
	@Override
    public String getString() throws IOException {
	    return Multiparts.readString(part);
    }

	@Override
	public byte[] getBytes() throws IOException {
		try(InputStream in = part.getInputStream()) {
			return IO.readByteArray(in);	
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return part.getInputStream();
	}

	@Override
	public void write(File dest) throws IOException, IllegalStateException {
		this.part.write(dest.getPath());
	}
}
