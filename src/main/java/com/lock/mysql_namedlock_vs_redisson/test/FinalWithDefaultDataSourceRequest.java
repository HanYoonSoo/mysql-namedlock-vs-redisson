package com.lock.mysql_namedlock_vs_redisson.test;

import com.lock.mysql_namedlock_vs_redisson.controller.UserController;
import com.lock.mysql_namedlock_vs_redisson.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * UserLevelLockFinal 방식과 기본풀을 이용한 UserLevelLock 호출
 */
@Slf4j
public class FinalWithDefaultDataSourceRequest {
    private static final int THREAD_COUNT = 30;

    public static void main(String[] args) {
        RequestUtil.concurrentPost(
                THREAD_COUNT,
                UserController.ADD_TICKET_URI_FINAL_WITH_DEFAULT_DATA_SOURCE,
                requestIndex -> new Object[]{requestIndex + 1L}
//                1L
        );
    }
}
