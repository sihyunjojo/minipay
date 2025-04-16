package org.c4marathon.assignment.domain.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.SavingAccount;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.common.jpa.EntityReferenceRepository;
import org.c4marathon.assignment.domain.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

	private final SavingAccountRepository savingAccountRepository;
	private final EntityReferenceRepository entityReferenceRepository;

	@Transactional
	public AccountDto createSavingAccountForMember(Long memberId, Long mainAccountId) {
		Member memberProxy = entityReferenceRepository.getMemberReference(memberId);
		MainAccount mainAccountProxy = entityReferenceRepository.getMainAccountReference(mainAccountId);

		SavingAccount savingAccount = SavingAccount.builder()
			.member(memberProxy)
			.balance(0L)
			.mainAccount(mainAccountProxy)
			.build();

		savingAccountRepository.save(savingAccount);

		return new AccountDto(savingAccount);
	}

	@Transactional
	public AccountDto createSavingAccountForMember(Long memberId, MainAccount mainAccount) {
		Member memberProxy = entityReferenceRepository.getMemberReference(memberId);

		SavingAccount savingAccount = SavingAccount.builder()
			.member(memberProxy)
			.balance(0L)
			.mainAccount(mainAccount)
			.build();

		savingAccountRepository.save(savingAccount);

		return new AccountDto(savingAccount);
	}
}
