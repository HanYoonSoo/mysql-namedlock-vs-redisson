package com.lock.mysql_namedlock_vs_redisson.test;

import com.lock.mysql_namedlock_vs_redisson.controller.UserController;
import com.lock.mysql_namedlock_vs_redisson.model.User;
import com.lock.mysql_namedlock_vs_redisson.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

/*
 * 한건씩 등록 요청
 */
@Slf4j
public class SingleThreadRequest {

    public static void main(String[] args) {
        for (int i = 0; i <= User.MAXIMUM_TIKET_COUNT; i++) {
            log.info("{} 번째 요청!!", i + 1);
            Integer count = RequestUtil.post(UserController.ADD_TICKET_URI, 1L);
            if (count != null) {
                log.info("response count : {}\n", count);
            }
        }
    }
}
