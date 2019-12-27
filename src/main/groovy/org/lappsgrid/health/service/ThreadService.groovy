package org.lappsgrid.health.service

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 */
@Service
@Slf4j("logger")
class ThreadService {

    ScheduledExecutorService executor

    public ThreadService() {
        int n = Runtime.getRuntime().availableProcessors()
        if (n > 2) {
            n = 2
        }
        executor = Executors.newScheduledThreadPool(n)
        logger.info("Created executor with {} threads", n)
    }

    void scheduleAtFixedRate(Runnable task, long delay, long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(task, delay, period, unit)
    }

    void schedule(Runnable task) {
        executor.schedule(task, 0, TimeUnit.MILLISECONDS)
    }
}
