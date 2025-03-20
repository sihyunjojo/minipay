package org.c4marathon.assignment.service;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.enums.AccountType;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    // @PersistenceContext를 사용하면 싱글톤이 아니라 트랜잭션 단위로 분리되어 사용되므로 여러 곳에서 안전하게 사용할 수 있다.
    @PersistenceContext
    private EntityManager entityManager;

    private final AccountRepository accountRepository;

    @Transactional
    public AccountDto createMainAccountForMember(Long memberId) {

        Member memberProxy = entityManager.getReference(Member.class, memberId);

        Account mainAccount = Account.builder()
                .member(memberProxy)
                .balance(0L)
                .accountType(AccountType.MAIN) // 메인 계좌 표시
                .build();

        accountRepository.save(mainAccount);

        return new AccountDto(mainAccount);
    }

    @Transactional
    public AccountDto createSavingAccountForMember(Long memberId) {
        validateMainAccountExists(memberId);

        Member memberProxy = entityManager.getReference(Member.class, memberId);

        Account savingAccount = Account.builder()
                .member(memberProxy)
                .balance(0L)
                .accountType(AccountType.SAVING)
                .build();

        accountRepository.save(savingAccount);

        return new AccountDto(savingAccount);
    }

    public void validateMainAccountExists(Long memberId) {
        Member memberProxy = entityManager.getReference(Member.class, memberId);

        boolean exists = accountRepository.existsByMemberAndAccountType(memberProxy, AccountType.MAIN);
        if (!exists) {
            throw new IllegalStateException("메인 계좌가 존재하지 않아 적금 계좌를 생성할 수 없습니다.");
        }
    }

}
