/*
 * Copyright 2011 Király Attila
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.captcha.cage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import leap.web.captcha.cage.Cage;
import leap.web.captcha.cage.GCage;
import leap.web.captcha.cage.YCage;
import leap.web.captcha.cage.image.EffectConfig;
import leap.web.captcha.cage.image.Painter;
import leap.web.captcha.cage.image.Painter.Quality;

import org.junit.Assert;
import org.junit.Test;

/**
 * General testing of the {@link Cage} and its components.
 * 
 * @author akiraly
 * 
 */
public class CageTest {

	/**
	 * Tests default settings.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDefault() throws IOException {
		testSize(new Cage());
	}

	/**
	 * Tests {@link Quality#MAX} setting.
	 * 
	 * @throws IOException
	 *             image could not be serialized
	 */
	@Test
	public void testMax() throws IOException {
		final Random rnd = new Random();
		testSize(new Cage(new Painter(Painter.DEFAULT_WIDTH,
				Painter.DEFAULT_HEIGHT, null, null, new EffectConfig(true,
						true, true, true, null), rnd), null, null, null,
				Cage.DEFAULT_COMPRESS_RATIO, null, rnd));
	}

	/**
	 * Tests G template.
	 * 
	 * @throws IOException
	 *             image could not be serialized
	 */
	@Test
	public void testG() throws IOException {
		testSize(new GCage());
	}

	/**
	 * Tests Y template.
	 * 
	 * @throws IOException
	 *             image could not be serialized
	 */
	@Test
	public void testY() throws IOException {
		testSize(new YCage());
	}

	/**
	 * Tests the passed object. Generates several captcha images and measures
	 * their size.
	 * 
	 * @param cage
	 *            object to be tested
	 * @throws IOException
	 *             image could not be serialized
	 */
	protected void testSize(Cage cage) throws IOException {
		for (int fi = 0; fi < 100; fi++) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			try {
				cage.draw("sometext", baos);
				Assert.assertTrue(
						"The image size is too small. Probably empty.",
						baos.size() > 1024);
			} finally {
				baos.close();
			}
		}
	}
}
