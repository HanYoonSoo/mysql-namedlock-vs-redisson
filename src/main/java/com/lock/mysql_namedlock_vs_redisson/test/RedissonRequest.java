package com.lock.mysql_namedlock_vs_redisson.test;

import com.lock.mysql_namedlock_vs_redisson.controller.UserController;
import com.lock.mysql_namedlock_vs_redisson.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Redisson Lock 호출
 */
@Slf4j
public class RedissonRequest {

    private static final int THREAD_COUNT = 30;

    public static void main(String[] args) {
        RequestUtil.concurrentPost(
                THREAD_COUNT,
                UserController.ADD_TICKET_URI_WITH_REDISSON,
//                requestIndex -> new Object[]{requestIndex + 1L},
                1
        );
    }
}
