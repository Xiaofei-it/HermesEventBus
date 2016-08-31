/**
 *
 * Copyright 2016 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xiaofei.library.hermeseventbus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xiaofei.library.concurrentutils.util.Action;
import xiaofei.library.concurrentutils.util.Function;

/**
 * Created by Xiaofei on 16/8/31.
 */
public class ObjectCanary2<T> {

    private volatile T object;

    private volatile AtomicInteger pendingTaskNumber;

    private final Lock lock;

    private final Condition condition;

    private final ExecutorService executor;

    public ObjectCanary2() {
        object = null;
        pendingTaskNumber = new AtomicInteger(0);
        lock = new ReentrantLock();
        condition = lock.newCondition();
        executor = Executors.newSingleThreadExecutor();
    }

    public void action(final Action<? super T> action) {
        if (object == null || pendingTaskNumber.get() > 0) {
            pendingTaskNumber.incrementAndGet();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (object == null) {
                        try {
                            lock.lock();
                            while (object == null) {
                                condition.await();
                            }
                            action.call(object);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        action.call(object);
                    }
                    pendingTaskNumber.decrementAndGet();
                }
            });
        } else {
            action.call(object);
        }
    }

    public <R> R calculate(final Function<? super T, ? extends R> function) {
        if (object == null || pendingTaskNumber.get() > 0) {
            pendingTaskNumber.incrementAndGet();
            Future<R> future = executor.submit(new Callable<R>() {
                @Override
                public R call() throws Exception {
                    R result = null;
                    if (object == null) {
                        try {
                            lock.lock();
                            while (object == null) {
                                condition.await();
                            }
                            result = function.call(object);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        result = function.call(object);
                    }
                    pendingTaskNumber.decrementAndGet();
                    return result;
                }
            });
            try {
                return future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return function.call(object);
        }
    }

    public void set(T object) {
        if (object == null) {
            throw new IllegalArgumentException("You cannot assign null to this object.");
        }
        lock.lock();
        this.object = object;
        condition.signalAll();
        lock.unlock();
    }

}
