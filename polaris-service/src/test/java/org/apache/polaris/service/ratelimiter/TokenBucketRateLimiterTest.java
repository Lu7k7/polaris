/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.polaris.service.ratelimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Main unit test class for TokenBucketRateLimiter */
public class TokenBucketRateLimiterTest {
  @Test
  void testBasic() {
    MockClock clock = new MockClock();
    clock.setSeconds(5);

    RateLimitResultAsserter asserter =
        new RateLimitResultAsserter(new TokenBucketRateLimiter(10, 100, clock));

    asserter.canAcquire(100);
    asserter.cantAcquire();

    clock.setSeconds(6);
    asserter.canAcquire(10);
    asserter.cantAcquire();

    clock.setSeconds(16);
    asserter.canAcquire(100);
    asserter.cantAcquire();
  }

  /**
   * Starts several threads that try to query the rate limiter at the same time, ensuring that we
   * only allow "maxTokens" requests
   */
  @Test
  void testConcurrent() throws InterruptedException {
    int maxTokens = 100;
    int numTasks = 50000;
    int tokensPerSecond = 10; // Can be anything above 0
    int sleepPerNThreads = 100; // Making this too low will result in the test taking a long time
    int maxSleepMillis = 5;

    TokenBucketRateLimiter rl =
        new TokenBucketRateLimiter(tokensPerSecond, maxTokens, new MockClock());
    AtomicInteger numAcquired = new AtomicInteger();
    CountDownLatch startLatch = new CountDownLatch(numTasks);
    CountDownLatch endLatch = new CountDownLatch(numTasks);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < numTasks; i++) {
        int i_ = i;
        executor.submit(
            () -> {
              try {
                // Enforce that tasks pause until all tasks are submitted
                startLatch.countDown();
                startLatch.await();

                // Make some threads sleep
                if (i_ % sleepPerNThreads == 0) {
                  Thread.sleep((int) (Math.random() * (maxSleepMillis + 1)));
                }

                if (rl.tryAcquire()) {
                  numAcquired.incrementAndGet();
                }
                endLatch.countDown();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            });
      }
    }

    endLatch.await();
    Assertions.assertEquals(maxTokens, numAcquired.get());
  }

  static class RateLimitResultAsserter {
    private final RateLimiter rateLimiter;

    RateLimitResultAsserter(RateLimiter rateLimiter) {
      this.rateLimiter = rateLimiter;
    }

    private void canAcquire(int times) {
      for (int i = 0; i < times; i++) {
        Assertions.assertTrue(rateLimiter.tryAcquire());
      }
    }

    private void cantAcquire() {
      for (int i = 0; i < 5; i++) {
        Assertions.assertFalse(rateLimiter.tryAcquire());
      }
    }
  }
}