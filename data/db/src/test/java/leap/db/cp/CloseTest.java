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

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;

public class CloseTest extends ConnPoolTestBase {

    private static CountDownLatch opened;
    private static CountDownLatch closed;

	@Test
	public void testCloseAll10_10() throws Exception {

		final int threads = 10;
		
		for(int i=0;i<3;i++) {
            openAndClose(10, threads);
			
			assertEquals(10, ms.getNrOfOpenedConnections());
			assertEquals(0, ms.getNrOfClosedConnections());
		}
		
		assertEquals(10, ms.getNrOfOpenedConnections());
		assertEquals(0, ms.getNrOfClosedConnections());
		
		ds.close();
		assertEquals(10, ms.getNrOfOpenedConnections());
		assertEquals(10, ms.getNrOfClosedConnections());
	}
	
	@Test
	public void testCloseAll10_15() throws Exception {
		ds.setMaxActive(10);
		
		final int threads = 15;
		
		for(int i=0;i<3;i++) {
            openAndClose(10, threads);

			assertEquals(10, ms.getNrOfOpenedConnections());
			assertEquals(0, ms.getNrOfClosedConnections());
		}
		
		assertEquals(10, ms.getNrOfOpenedConnections());
		assertEquals(0, ms.getNrOfClosedConnections());
		
		ds.close();
		assertEquals(10, ms.getNrOfOpenedConnections());
		assertEquals(10, ms.getNrOfClosedConnections());
	}
	
	@Test
	public void testCloseAll10_20() throws Exception {
		initDefaultDataSource();

		ds.setMaxActive(10);

		final int threads = 20;

		for(int i=0;i<3;i++) {
            openAndClose(10, threads);

			assertEquals(10, ms.getNrOfOpenedConnections());
			assertEquals(0, ms.getNrOfClosedConnections());
		}

		assertEquals(10, ms.getNrOfOpenedConnections());
		assertEquals(0, ms.getNrOfClosedConnections());

		ds.close();
		assertEquals(10, ms.getNrOfOpenedConnections());
		assertEquals(10, ms.getNrOfClosedConnections());
	}

    private void openAndClose(int conns, int threads) throws Exception {
        opened = new CountDownLatch(conns);
        closed = new CountDownLatch(threads);

        for(int j=0;j<threads;j++) {
            OpenCloseThread thread = new OpenCloseThread(ds);
            thread.start();
        }

        //wait for all closed.
        closed.await();
    }
	
	private final class OpenCloseThread extends Thread {
		
		private final DataSource ds;

		public OpenCloseThread(DataSource ds) {
			this.ds = ds;
		}

		@Override
        public void run() {
			try {
	            try(Connection conn = ds.getConnection()) {
                    opened.countDown();

                    //wait for all opened.
                    opened.await();
	            }
            } catch (Exception e) {
	            e.printStackTrace();
            } finally {
            	closed.countDown();
            }
		}
		
	}
}
