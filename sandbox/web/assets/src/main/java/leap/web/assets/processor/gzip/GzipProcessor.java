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
package leap.web.assets.processor.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import leap.lang.io.IO;
import leap.web.assets.AssetManager;
import leap.web.assets.AssetFile;
import leap.web.assets.AssetProcessor;

public class GzipProcessor implements AssetProcessor {

	@Override
	public void process(AssetManager manager, AssetFile file) throws IOException {
		if(!file.isGzipped()){
			if(file.isMinified()){
				file.setGzippedContent(gzip(file.getMinifiedContent()));
			}else{
				file.setGzippedContent(gzip(file.getTargetContent()));
			}
		}
	}

	protected byte[] gzip(byte[] data) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		GZIPOutputStream out = null;
		try{
			out = new GZIPOutputStream(bao);
			out.write(data);
			out.finish();
			
			return bao.toByteArray();
		}finally{
			IO.close(out);
		}
	}
}
