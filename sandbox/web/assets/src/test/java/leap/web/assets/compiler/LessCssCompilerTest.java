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
package leap.web.assets.compiler;

import java.io.IOException;

import leap.core.junit.AppTestBase;
import leap.junit.concurrent.Concurrent;
import leap.lang.Strings;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.web.assets.AssetCompileException;
import leap.web.assets.compiler.LessCssCompiler;

import org.junit.Test;

public class LessCssCompilerTest extends AppTestBase {
	
	private LessCssCompiler compiler = new LessCssCompiler();

	@Test
	public void testHelloLess(){
		String source = 
				"@color: #4d926f;\n" + 
				"#header{color: @color;}";
		
		String compiled = 
				"#header{color:#4d926f;}";
		
		assertEquals(compiled, Strings.removeBlank(compiler.compileCss(source)));
	}
	
	@Test
	@Concurrent
	public void testHelloImportCss(){
		assertEquals("@import 'a.css';",Strings.trim(compiler.compileCss("@import 'a.css';")));
	}
	
	@Test
	@Concurrent
	public void testHelloImportLess() throws IOException{
		Resource r = Resources.getResource("classpath:/less/a.less");
		assertEquals("#header{color:#ff0000;}", Strings.removeBlank(compiler.compileCss(r.getFile())));
	}
	
	@Test
	public void testCompileError(){
		try{
			compiler.compileCss("#header{.a}");
			fail("should throw exception");
		}catch(AssetCompileException e){
			//assertEquals(1, e.getLine());
			//assertTrue(e.getMessage().contains(".a"));
			assertNotEmpty(e.getMessage());
		}
	}
}