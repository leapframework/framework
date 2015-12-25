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
package leap.db.cp;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import leap.lang.Randoms;

import org.junit.Test;

public class ConnectionCloseTest extends ConnPoolTestBase {

	@Test
	public void testCloseAll10_10() throws Exception {
		poolds.setMaxActive(10);
		
		final int threads = 10;
		
		for(int i=0;i<3;i++) {
			CountDownLatch latch = new CountDownLatch(threads);
			
			for(int j=0;j<threads;j++) {
				OpenCloseThread thread = new OpenCloseThread(poolds, latch);
				thread.start();
			}
			
			latch.await();
			
			assertEquals(10,mockds.getNrOfOpenedConnections());
			assertEquals(0, mockds.getNrOfClosedConnections());
		}
		
		assertEquals(10,mockds.getNrOfOpenedConnections());
		assertEquals(0, mockds.getNrOfClosedConnections());
		
		poolds.close();
		assertEquals(10,mockds.getNrOfOpenedConnections());
		assertEquals(10,mockds.getNrOfClosedConnections());
	}
	
	@Test
	public void testCloseAll10_15() throws Exception {
		poolds.setMaxActive(10);
		
		final int threads = 15;
		
		for(int i=0;i<3;i++) {
			CountDownLatch latch = new CountDownLatch(threads);
			
			for(int j=0;j<threads;j++) {
				OpenCloseThread thread = new OpenCloseThread(poolds, latch);
				thread.start();
			}
			
			latch.await();
			
			assertEquals(10,mockds.getNrOfOpenedConnections());
			assertEquals(0, mockds.getNrOfClosedConnections());
		}
		
		assertEquals(10,mockds.getNrOfOpenedConnections());
		assertEquals(0, mockds.getNrOfClosedConnections());
		
		poolds.close();
		assertEquals(10,mockds.getNrOfOpenedConnections());
		assertEquals(10,mockds.getNrOfClosedConnections());
	}
	
	@Test
	public void testCloseAll10_20() throws Exception {
		initDefaultDataSource();
		
		poolds.setMaxActive(10);
		
		final int threads = 20;
		
		for(int i=0;i<3;i++) {
			CountDownLatch latch = new CountDownLatch(threads);
			
			for(int j=0;j<threads;j++) {
				OpenCloseThread thread = new OpenCloseThread(poolds, latch);
				thread.start();
			}
			
			latch.await();
			
			assertEquals(10,mockds.getNrOfOpenedConnections());
			assertEquals(0, mockds.getNrOfClosedConnections());
		}
		
		assertEquals(10,mockds.getNrOfOpenedConnections());
		assertEquals(0, mockds.getNrOfClosedConnections());
		
		poolds.close();
		assertEquals(10,mockds.getNrOfOpenedConnections());
		assertEquals(10,mockds.getNrOfClosedConnections());
	}
	
	private final class OpenCloseThread extends Thread {
		
		private final DataSource     ds;
		private final CountDownLatch latch;
		
		public OpenCloseThread(DataSource ds, CountDownLatch latch) {
			this.ds = ds;
			this.latch = latch;
		}

		@Override
        public void run() {
			try {
	            try(Connection conn = ds.getConnection()) {
	            	Thread.sleep(Randoms.nextInt(10, 50));
	            }
            } catch (Exception e) {
	            e.printStackTrace();
            } finally {
            	latch.countDown();
            }
		}
		
	}
}
