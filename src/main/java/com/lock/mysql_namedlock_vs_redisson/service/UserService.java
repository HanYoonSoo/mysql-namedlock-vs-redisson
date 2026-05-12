package com.lock.mysql_namedlock_vs_redisson.service;

import com.lock.mysql_namedlock_vs_redisson.model.Ticket;
import com.lock.mysql_namedlock_vs_redisson.model.User;
import com.lock.mysql_namedlock_vs_redisson.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public int addNewTicket(Long userId) {
        return addNewTicketInternal(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int addNewTicketRequiresNew(Long userId) {
        return addNewTicketInternal(userId);
    }

    private int addNewTicketInternal(Long userId) {
        log.info("user 조회");
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

        log.info("카드 추가");
        String serial = UUID.randomUUID().toString();
        user.addTicket(new Ticket(serial));
        userRepository.saveAndFlush(user);

        log.info("추가 완료");
        int cardCount = user.getTicketCount();

        log.info("cardCount : {}", cardCount);
        return cardCount;
    }
}
