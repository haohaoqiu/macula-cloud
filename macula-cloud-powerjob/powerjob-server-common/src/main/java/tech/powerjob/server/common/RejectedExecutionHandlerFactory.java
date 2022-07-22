package tech.powerjob.server.common;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拒绝策略
 *
 * @author tjq
 * @since 2020/11/28
 */
@Slf4j
public class RejectedExecutionHandlerFactory {

    private static final AtomicLong COUNTER = new AtomicLong();

    /**
     * 直接丢弃该任务
     *
     * @param source log name
     * @return A handler for tasks that cannot be executed by ThreadPool
     */
    public static RejectedExecutionHandler newReject(String source) {
        return (r, p) -> {
            log.error("[{}] ThreadPool[{}] overload, the task[{}] will be dropped!", source, p, r);
            log.warn("[{}] Maybe you need to adjust the ThreadPool config!", source);
        };
    }

    /**
     * 调用线程运行
     *
     * @param source log name
     * @return A handler for tasks that cannot be executed by ThreadPool
     */
    public static RejectedExecutionHandler newCallerRun(String source) {
        return (r, p) -> {
            log.warn("[{}] ThreadPool[{}] overload, the task[{}] will run by caller thread!", source, p, r);
            log.warn("[{}] Maybe you need to adjust the ThreadPool config!", source);
            if (!p.isShutdown()) {
                r.run();
            }
        };
    }

    /**
     * 新线程运行
     *
     * @param source log name
     * @return A handler for tasks that cannot be executed by ThreadPool
     */
    public static RejectedExecutionHandler newThreadRun(String source) {
        return (r, p) -> {
            log.warn("[{}] ThreadPool[{}] overload, the task[{}] will run by a new thread!", source, p, r);
            log.warn("[{}] Maybe you need to adjust the ThreadPool config!", source);
            if (!p.isShutdown()) {
                String threadName = source + "-T-" + COUNTER.getAndIncrement();
                log.info("[{}] create new thread[{}] to run job", source, threadName);
                new Thread(r, threadName).start();
            }
        };
    }

}
