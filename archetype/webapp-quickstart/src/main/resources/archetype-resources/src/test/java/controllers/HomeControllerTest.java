package ${package}.controllers;

import leap.lang.Strings;
import leap.lang.http.HttpStatus;
import leap.lang.http.MimeTypes;
import leap.web.WebTestCase;

import org.junit.Test;

public class HomeControllerTest extends WebTestCase {
	
	@Test
	public void testIndex(){
		response = get("");
		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>It works!</h1>", response.getString());
	}

}