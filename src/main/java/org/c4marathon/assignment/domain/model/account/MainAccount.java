package org.c4marathon.assignment.domain.model.account;

import org.c4marathon.assignment.domain.model.Member;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainAccount implements Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private Long balance;
    private Long dailyChargeAmount = 0L;

    @Builder
    public MainAccount(Member member, Long balance) {
        this.member = member;
        this.balance = balance;
    }

    @Override
    public void deposit(Long amount) {
        this.balance += amount;
    }

    @Override
    public void withdraw(Long amount) {
        if (amount <= 0) throw new IllegalArgumentException("출금액은 0보다 커야 합니다");
        if (this.balance < amount) throw new IllegalStateException("잔액 부족");
        this.balance -= amount;
    }
}
