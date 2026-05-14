package com.lock.mysql_namedlock_vs_redisson.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@ToString
@Table(name = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    public static final int MAXIMUM_TICKET_COUNT = 1_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "user",cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Ticket> tickets = new HashSet<>();

    public User(String name) {
        this.name = name;
    }

    public void addTicket(Ticket ticket) {
        if (isMaxTicketCount()) {
            throw new IllegalStateException("최대 수량을 초과하였습니다. 최대수량 : "+ MAXIMUM_TICKET_COUNT +", 현재 크기 : "+ getTicketCount());
        }
        ticket.setUser(this);
        this.tickets.add(ticket);
    }

    private boolean isMaxTicketCount() {
        return getTicketCount() >= MAXIMUM_TICKET_COUNT;
    }

    public int getTicketCount() {
        return this.tickets.size();
    }

}
