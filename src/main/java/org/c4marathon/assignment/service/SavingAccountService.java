package org.c4marathon.assignment.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.domain.account.SavingAccount;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

	@PersistenceContext
	private EntityManager entityManager;

	private final SavingAccountRepository savingAccountRepository;

	@Transactional
	public AccountDto createSavingAccountForMember(Long memberId, Long mainAccountId) {
		Member memberProxy = entityManager.getReference(Member.class, memberId);
		MainAccount mainAccountProxy = entityManager.getReference(MainAccount.class, mainAccountId);

		SavingAccount savingAccount = SavingAccount.builder()
			.member(memberProxy)
			.balance(0L)
			.mainAccount(mainAccountProxy)
			.build();

		savingAccountRepository.save(savingAccount);

		return new AccountDto(savingAccount);
	}

	@Transactional
	public AccountDto createSavingAccountForMember2(Long memberId, MainAccount mainAccount) {
		Member memberProxy = entityManager.getReference(Member.class, memberId);
		// MainAccount mainAccountProxy = entityManager.getReference(MainAccount.class, mainAccountId);

		SavingAccount savingAccount = SavingAccount.builder()
			.member(memberProxy)
			.balance(0L)
			.mainAccount(mainAccount)
			.build();

		savingAccountRepository.save(savingAccount);

		return new AccountDto(savingAccount);
	}
}
