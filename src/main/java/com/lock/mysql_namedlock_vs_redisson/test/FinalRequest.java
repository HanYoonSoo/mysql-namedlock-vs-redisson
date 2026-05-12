package com.lock.mysql_namedlock_vs_redisson.test;

import com.lock.mysql_namedlock_vs_redisson.controller.UserController;
import com.lock.mysql_namedlock_vs_redisson.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 최종 버전 UserLevelLock 호출
 */
@Slf4j
public class FinalRequest {

    private static final int THREAD_COUNT = 31;

    public static void main(String[] args) {
        RequestUtil.concurrentPost(THREAD_COUNT, UserController.ADD_TICKET_URI_FINAL, 1L);
    }

}

