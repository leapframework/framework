/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.lang;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Threads {

    public static void sleep(long ms) {
        Try.throwUnchecked(() -> Thread.sleep(ms));
    }

    public static <T> T waitAndGet(Supplier<T> func) throws TimeoutException {
        return waitAndGet(func, 1000L);
    }

    public static <T> T waitAndGet(Supplier<T> func, long maxWait) throws TimeoutException {
        long timeout = maxWait;
        final long start = System.currentTimeMillis();

        do{
            T item = func.get();
            if(null != item) {
                return item;
            }

            //decrease the timeout
            timeout = maxWait - (System.currentTimeMillis() - start);
            if(timeout <= 0L) {
                //time out.
                break;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }

        }while(timeout > 0);

        throw new TimeoutException("timeout");
    }

    protected Threads() {

    }
}
