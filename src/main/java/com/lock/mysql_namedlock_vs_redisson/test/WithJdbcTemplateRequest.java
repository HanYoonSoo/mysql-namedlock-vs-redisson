package com.lock.mysql_namedlock_vs_redisson.test;

import com.lock.mysql_namedlock_vs_redisson.controller.UserController;
import com.lock.mysql_namedlock_vs_redisson.util.RequestUtil;

/**
 * JdbcTemplate 을 이용한 UserLevelLock 호출
 */
public class WithJdbcTemplateRequest {

    private static final int THREAD_COUNT = 20;

    public static void main(String[] args) {
        RequestUtil.concurrentPost(THREAD_COUNT, UserController.ADD_TICKET_URI_WITH_TEMPLATE, 1L);
    }
}
