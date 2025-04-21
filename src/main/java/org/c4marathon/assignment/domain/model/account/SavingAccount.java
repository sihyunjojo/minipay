package org.c4marathon.assignment.domain.model.account;

import org.c4marathon.assignment.domain.model.Member;

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
}
