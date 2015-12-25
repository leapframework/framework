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
package app1.controllers;

import java.io.File;
import java.net.URLEncoder;

import leap.lang.io.Files;
import leap.lang.io.IO;
import leap.web.WebTestCase;

import org.junit.Test;

public class AssetPublishTest extends WebTestCase {
	
    @Test
    @SuppressWarnings("deprecation")
	public void testPublishOnRequest() {
		File tmpdir = new File(IO.tmpdir());
		
		File pubdir = Files.createRelative(tmpdir, "app1/assets");
		if(pubdir.exists()) {
			pubdir.delete();
		}
		
		String assetPath = get("/app1/asset_client_url?path=" + URLEncoder.encode("/css/style.css")).getContent();
		
		File file = Files.createRelative(tmpdir, assetPath);
		assertTrue(file.exists());
	}

}