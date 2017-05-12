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
package app.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import leap.core.validation.annotations.NotEmpty;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.web.Content;
import leap.web.annotation.Path;
import leap.web.download.Download;
import leap.web.download.InputStreamDownload;
import leap.web.download.ServletResourceDownload;

public class DownloadController {
	
	public Content test() {
		return new ServletResourceDownload("/WEB-INF/download/test.txt");
	}
	
	public String test1() {
		return "download:/WEB-INF/download/test.txt";
	}
	
	public Content test2() {
		ServletResourceDownload dl = new ServletResourceDownload("/WEB-INF/download/test.txt");
		dl.setFilename("中文文件.txt");
		return dl;
	}
	
	public Download test3() {
	    byte[] data = Strings.getBytesUtf8("中文流");
	    InputStream in = new ByteArrayInputStream(data);
	    return new InputStreamDownload(in,"stream.txt", data.length);
	}

	@Path("any/{file:.*}")
	public Content any(@NotEmpty String file) {
		return new ServletResourceDownload("/WEB-INF/download" + Paths.prefixWithSlash(file));
	}
	
	
}
