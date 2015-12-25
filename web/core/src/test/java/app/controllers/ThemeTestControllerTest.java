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
package app.controllers;

import org.junit.Test;

import leap.web.WebTestCase;

public class ThemeTestControllerTest extends WebTestCase {

	@Test
	public void testThemeMessageSource() {
		get("theme_test/msg?key=theme_test").assertContentEquals("Theme");
	}
	
	@Test
	public void testThemeAssetSource(){
		get("assets/js/theme_test.js").assertContentEquals("ok");
	}
	
	@Test
	public void testThemeViewSource() {
		get("test_theme").assertContentEquals("ok");
	}
	
	@Test
	public void testViewOnlyInTheme() {
		get("theme_test/test1").assertContentEquals("ok");
	}

}