package org.c4marathon.assignment.domain.model.account;

import org.c4marathon.assignment.domain.model.Member;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Table(name = "main_account", uniqueConstraints = {
	@UniqueConstraint(columnNames = "member_id")
}) // 똑같은 회원에 대해 중복 생성 방지
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

	public void setMember(Member member) {
		this.member = member;
		if (member != null && member.getMainAccount() != this) {
			member.setMainAccount(this);
		}
	}

	@Version
	private Long version;
}
