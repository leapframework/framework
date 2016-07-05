/**
 * Copyright (C) 2010 Mycila <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.junit.concurrent;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ConcurrentRunnerException extends RuntimeException {
    private static final long serialVersionUID = 4401596464149916841L;

	private ConcurrentRunnerException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public Throwable unwrap() {
        Throwable t = getCause();
        while (t instanceof ConcurrentRunnerException)
            t = t.getCause();
        return t;
    }

    public static ConcurrentRunnerException wrap(Throwable t) {
        if (t instanceof ConcurrentRunnerException)
            t = ((ConcurrentRunnerException) t).unwrap();
        ConcurrentRunnerException concurrentException = new ConcurrentRunnerException(t);
        concurrentException.setStackTrace(t.getStackTrace());
        return concurrentException;
    }

}
