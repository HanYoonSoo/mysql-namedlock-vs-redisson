package com.lock.mysql_namedlock_vs_redisson.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(name = "ticket")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "serial", nullable = false)
    private String serial;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Ticket(String serial) {
        this.serial = serial;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
