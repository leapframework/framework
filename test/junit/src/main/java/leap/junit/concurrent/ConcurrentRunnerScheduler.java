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

import org.junit.runners.model.RunnerScheduler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ConcurrentRunnerScheduler implements RunnerScheduler {

    private static final int CPUS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executorService;
    private final Queue<Future<Void>> tasks = new LinkedList<Future<Void>>();
    private final CompletionService<Void> completionService;

    public ConcurrentRunnerScheduler(String name, int threads) {
        this(name, Math.min(CPUS, threads), Math.max(CPUS, threads));
    }

    public ConcurrentRunnerScheduler(String name, int nThreadsMin, int nThreadsMax) {
        executorService = new ThreadPoolExecutor(
            nThreadsMin, nThreadsMax,
            10L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new NamedThreadFactory(name),
            new ThreadPoolExecutor.CallerRunsPolicy());
        completionService = new ExecutorCompletionService<Void>(executorService);
    }

    public void schedule(Runnable childStatement) {
        tasks.offer(completionService.submit(childStatement, null));
    }

    public void finished() throws ConcurrentRunnerException {
        try {
            while (!tasks.isEmpty()) {
                Future<Void> f = completionService.take();
                tasks.remove(f);
                f.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw ConcurrentRunnerException.wrap(e.getCause());
        } finally {
            while (!tasks.isEmpty())
                tasks.poll().cancel(true);
            executorService.shutdownNow();
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final ThreadGroup group;

        NamedThreadFactory(String poolName) {
            group = new ThreadGroup(poolName + " Group-" + poolNumber.getAndIncrement());
        }

        public Thread newThread(Runnable r) {
            return new Thread(group, r, group.getName() + "-Thread-" + threadNumber.getAndIncrement(), 0);
        }
    }
}