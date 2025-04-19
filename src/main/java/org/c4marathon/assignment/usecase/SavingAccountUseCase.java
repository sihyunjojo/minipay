package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.MemberService;
import org.c4marathon.assignment.domain.service.SavingAccountService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

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
}
