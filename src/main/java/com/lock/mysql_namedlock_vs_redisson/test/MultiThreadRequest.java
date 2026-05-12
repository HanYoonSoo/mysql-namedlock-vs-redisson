package com.lock.mysql_namedlock_vs_redisson.test;

import com.lock.mysql_namedlock_vs_redisson.controller.UserController;
import com.lock.mysql_namedlock_vs_redisson.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 동시에 등록 요청
 */
@Slf4j
public class MultiThreadRequest {

    private static final int THREAD_COUNT = 20;

    public static void main(String[] args) {
        RequestUtil.concurrentPost(THREAD_COUNT, UserController.ADD_TICKET_URI, 1L);
    }

}
