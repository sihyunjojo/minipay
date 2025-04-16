package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.domain.service.MainAccountService;
import org.c4marathon.assignment.domain.service.MemberService;
import org.c4marathon.assignment.domain.service.SavingAccountService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountUseCase {

	private final MemberService memberService;
	private final MainAccountService mainAccountService;
	private final SavingAccountService savingAccountService;

	@Transactional
	public AccountDto registerSavingAccount(Long memberId) {
		memberService.validateMemberExists(memberId);
		Long memberAccountId = mainAccountService.getMainAccountByMemberId(memberId);
		return savingAccountService.createSavingAccountForMember(memberId, memberAccountId);
	}

	@Transactional
	public AccountDto registerSavingAccount2(Long memberId) {
		memberService.validateMemberExists(memberId);
		MainAccount mainAccount = mainAccountService.getMainAccountByMemberId2(memberId);
		return savingAccountService.createSavingAccountForMember(memberId, mainAccount);
	}
}
