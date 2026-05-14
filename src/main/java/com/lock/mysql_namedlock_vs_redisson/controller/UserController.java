package com.lock.mysql_namedlock_vs_redisson.controller;

import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockFinal;
import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockFinalWithDefaultDataSource;
import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockWithRedisson;
import com.lock.mysql_namedlock_vs_redisson.lock.UserLevelLockWithJdbcTemplate;
import com.lock.mysql_namedlock_vs_redisson.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    public static final String ADD_TICKET_URI = "http://localhost:8080/users/{userId}/add-new-ticket";
    public static final String ADD_TICKET_URI_WITH_TEMPLATE = "http://localhost:8080/users/{userId}/add-new-ticket-with-template";
    public static final String ADD_TICKET_URI_FINAL = "http://localhost:8080/users/{userId}/add-new-ticket-final";
    public static final String ADD_TICKET_URI_FINAL_WITH_DEFAULT_DATA_SOURCE = "http://localhost:8080/users/{userId}/add-new-ticket-final-with-default-data-source";
    public static final String ADD_TICKET_URI_WITH_REDISSON = "http://localhost:8080/users/{userId}/add-new-ticket-with-redisson";

    private static final int LOCK_TIMEOUT_SECONDS = 3;

    private final UserService userService;
    private final UserLevelLockWithJdbcTemplate userLevelLockWithJdbcTemplate;
    private final UserLevelLockFinal userLevelLockFinal;
    private final UserLevelLockFinalWithDefaultDataSource userLevelLockFinalWithDefaultDataSource;
    private final UserLevelLockWithRedisson userLevelLockWithRedisson;

    /**
     * USER LEVEL LOCK 사용 하지 않는다.
     */
    @PostMapping("/{userId}/add-new-ticket")
    public int addNewTicket(@PathVariable Long userId) {
        return userService.addNewTicket(userId);
    }

    /**
     * JdbcTemplate 으로 구현한 버전 사용.
     */
    @PostMapping("/{userId}/add-new-ticket-with-template")
    public int addNewTicketWithTemplate(@PathVariable Long userId) {
        return userLevelLockWithJdbcTemplate.executeWithLock(
                String.valueOf(userId),
                LOCK_TIMEOUT_SECONDS,
                () -> userService.addNewTicket(userId)
        );
    }

    /**
     * 최종 버전 사용.
     */
    @PostMapping("/{userId}/add-new-ticket-final")
    public int addNewTicketFinal(@PathVariable Long userId) {
        return userLevelLockFinal.executeWithLock(
                String.valueOf(userId),
                LOCK_TIMEOUT_SECONDS,
                () -> userService.addNewTicket(userId)
        );
    }

    /**
     * 최종 버전과 동일한 방식으로 기본 커넥션 풀을 사용하는 버전.
     */
    @PostMapping("/{userId}/add-new-ticket-final-with-default-data-source")
    public int addNewTicketFinalWithDefaultDataSource(@PathVariable Long userId) {
        return userLevelLockFinalWithDefaultDataSource.executeWithLock(
                String.valueOf(userId),
                LOCK_TIMEOUT_SECONDS,
                () -> userService.addNewTicketRequiresNew(userId)
        );
    }

    /**
     * Redisson Lock 을 사용하는 버전.
     */
    @PostMapping("/{userId}/add-new-ticket-with-redisson")
    public int addNewTicketWithRedisson(@PathVariable Long userId) {
        return userLevelLockWithRedisson.executeWithLock(
                String.valueOf(userId),
                LOCK_TIMEOUT_SECONDS,
                () -> userService.addNewTicket(userId)
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleError(IllegalStateException exception) {
        String message = exception.getMessage();
        log.warn(message);
        return message;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        String message = formatExceptionMessage(exception);
        log.error(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    private String formatExceptionMessage(RuntimeException exception) {
        Throwable rootCause = getRootCause(exception);
        if (rootCause == exception) {
            return exception.getClass().getSimpleName() + ": " + exception.getMessage();
        }
        return exception.getClass().getSimpleName() + ": " + exception.getMessage()
                + ", rootCause=" + rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage();
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable result = throwable;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }
}
