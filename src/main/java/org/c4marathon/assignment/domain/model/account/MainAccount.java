package org.c4marathon.assignment.domain.model.account;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Table(name = "main_account", uniqueConstraints = {
	@UniqueConstraint(columnNames = "member_id")
}) // 똑같은 회원에 대해 중복 생성 방지
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainAccount implements Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@OneToMany(mappedBy = "fromMainAccount", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TransferTransaction> sentTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "toMainAccount", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TransferTransaction> receivedTransactions = new ArrayList<>();

	private Long balance;
	private Long dailyChargeAmount = 0L;

	@Version
	private Long version;

	@Builder
	public MainAccount(Member member, Long balance) {
		this.member = member;
		this.balance = balance;
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

	// 받은 거래 추가
	public void addReceivedTransaction(TransferTransaction transaction) {
		receivedTransactions.add(transaction);
		if (transaction != null && transaction.getToMainAccount() != this) {
			transaction.setToMainAccount(this);
		}
	}

	// 보낸 거래 제거
	public void removeSentTransaction(TransferTransaction transaction) {
		sentTransactions.remove(transaction);
		transaction.setFromMainAccount(null);
	}

	// 받은 거래 제거
	public void removeReceivedTransaction(TransferTransaction transaction) {
		receivedTransactions.remove(transaction);
		transaction.setToMainAccount(null);
	}

	public static MainAccount of(Long id) {
		MainAccount account = new MainAccount();
		account.setId(id);
		return account;
	}

}
