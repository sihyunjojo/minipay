package org.c4marathon.assignment.domain.account;

import org.c4marathon.assignment.domain.Member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainAccount implements Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public void increaseDailyCharge(Long amount) {
        this.dailyChargeAmount += amount;
    }

    public void resetDailyChargeAmount() {
        this.dailyChargeAmount = 0L;
    }
}
