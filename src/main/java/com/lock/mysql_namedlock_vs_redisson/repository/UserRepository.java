package com.lock.mysql_namedlock_vs_redisson.repository;

import com.lock.mysql_namedlock_vs_redisson.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
