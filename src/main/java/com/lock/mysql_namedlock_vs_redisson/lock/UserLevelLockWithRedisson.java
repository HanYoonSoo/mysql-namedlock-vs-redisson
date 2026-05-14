package com.lock.mysql_namedlock_vs_redisson.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class UserLevelLockWithRedisson {

    private static final String LOCK_PREFIX = "user:";
    private static final String EXCEPTION_MESSAGE = "LOCK 을 수행하는 중에 오류가 발생하였습니다.";

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(String userLockName, int timeoutSeconds, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + userLockName);
        boolean locked = false;

        try {
            locked = lock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException(EXCEPTION_MESSAGE + " type=TryLock, userLockName=" + userLockName);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(EXCEPTION_MESSAGE + " type=Interrupted, userLockName=" + userLockName, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
