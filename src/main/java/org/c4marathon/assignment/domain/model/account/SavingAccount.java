package org.c4marathon.assignment.domain.model.account;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.account.enums.SavingType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SavingAccount implements Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Builder.Default
	private Long balance = 0L;

	@Builder.Default
	@Column(nullable = true)
	private Long subscribedDepositAmount = 0L;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SavingType savingType;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "main_account_id")
	private MainAccount mainAccount;

	@Version
	Long version;

	public static SavingAccount createFixedSavingAccount(Member member, MainAccount mainAccount, Long subscribedDepositAmount) {
		SavingAccount savingAccount = SavingAccount.builder()
			.mainAccount(mainAccount)
			.member(member)
			.savingType(SavingType.FIXED)
			.subscribedDepositAmount(subscribedDepositAmount)
			.build();

		mainAccount.addSavingAccount(savingAccount);
		member.addSavingAccount(savingAccount);

		return savingAccount;
	}

	public static SavingAccount createFlexibleSavingAccount(Member member, MainAccount mainAccount) {
		SavingAccount savingAccount = SavingAccount.builder()
			.mainAccount(mainAccount)
			.member(member)
			.savingType(SavingType.FLEXIBLE)
			.build();

		mainAccount.addSavingAccount(savingAccount);
		member.addSavingAccount(savingAccount);

		return savingAccount;
	}

	public void deposit(Long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
		}
		this.balance += amount;
	}

	/**
	 * 매일 지급할 이자 계산 (단리 기준)
	 * 은행/저축은행/카카오뱅크, 토스 등 실제 약관들을 보면 절삭(버림) 방식이 일반적
	 */
	public Long calculateInterest(Double rate) {
		return (long) (this.balance * rate); // 소수점 절삭
	}
}
