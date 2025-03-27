package org.c4marathon.assignment.domain.account;

import org.c4marathon.assignment.domain.Member;

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
        if (this.balance < amount) throw new IllegalStateException("잔액 부족");
        this.balance -= amount;
    }

    public void charge(Long amount) {
        this.balance += amount;
        increaseDailyCharge(amount);
    }

    private void increaseDailyCharge(Long amount) {
        this.dailyChargeAmount += amount;
    }
}
