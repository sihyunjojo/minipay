package org.c4marathon.assignment.domain.model.account;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Table(name = "main_account", uniqueConstraints = {
	@UniqueConstraint(columnNames = "account_number"),
	@UniqueConstraint(columnNames = "member_id")
})
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainAccount implements Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "account_number", nullable = false, unique = true, length = 20)
	private String accountNumber;

	private Long balance;
	private Long dailyChargeAmount;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 서로 Setter만 잘되어 있으면, emberRepository.save(member)만으로 account까지 자동 저장
	@OneToMany(mappedBy = "mainAccount", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SavingAccount> savingAccounts = new ArrayList<>();

	@OneToMany(mappedBy = "fromMainAccount", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TransferTransaction> sentTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "toMainAccount", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TransferTransaction> receivedTransactions = new ArrayList<>();

	@Version
	private Long version;

	@Builder(access = AccessLevel.PRIVATE)
	private MainAccount(String accountNumber, Long balance, Long dailyChargeAmount, Member member) {
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.dailyChargeAmount = dailyChargeAmount;
		this.member = member;
	}

	//----FACTORY METHOD START----
	public static MainAccount create( Member member, String accountNumber) {
		if (member == null) throw new IllegalArgumentException("회원 정보는 필수입니다.");
		return MainAccount.builder()
			.accountNumber(accountNumber)
			.balance(0L)
			.dailyChargeAmount(0L)
			.member(member)
			.build();
	}

	public void setMember(Member member) {
		this.member = member;
		if (member != null && member.getMainAccount() != this) {
			member.setMainAccount(this);
		}
	}

	// 보낸 거래 추가
	public void addSentTransaction(TransferTransaction transaction) {
		sentTransactions.add(transaction);
		if (transaction != null && transaction.getFromMainAccount() != this) {
			transaction.setFromMainAccount(this);
		}
	}

	public void addSavingAccount(SavingAccount savingAccount) {
		savingAccounts.add(savingAccount);
		if (savingAccount != null && savingAccount.getMainAccount() != this) {
			savingAccount.setMainAccount(this);
		}
	}

	// 받은 거래 추가
	public void addReceivedTransaction(TransferTransaction transaction) {
		receivedTransactions.add(transaction);
		if (transaction != null && transaction.getToMainAccount() != this) {
			transaction.setToMainAccount(this);
		}
	}
}
