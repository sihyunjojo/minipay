package org.c4marathon.assignment.domain.account;

import org.c4marathon.assignment.domain.Member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavingAccount implements Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "main_account_id")
	private MainAccount mainAccount;

	private Long balance;

	@Builder
	public SavingAccount(Member member, Long balance, MainAccount mainAccount) {
		this.member = member;
		this.balance = balance;
		this.mainAccount = mainAccount;
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
}
