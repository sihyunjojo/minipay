package org.c4marathon.assignment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.c4marathon.assignment.domain.common.BaseTimeEntity;
import org.c4marathon.assignment.domain.enums.AccountType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Step 1. 계좌 세팅
// 구현사항
// 계좌를 생성합시다.
// 사용자 정보는 평범하게 구현하고, (인증 여부는 하고 싶은대로 하면 됩니다.) 각 사용자가 여러 계좌를 생성할 수 있게 만들어야 합니다.
// 회원 등록 시, 본인의 "메인 계좌" 가 생성이 됩니다.
// 이 계좌는 외부 계좌에서 돈을 가져오는 기능이 주 기능이므로, 금액 추가가 가능합니다.
// 다만, 인당 1일 출전 한도는 3,000,000원 입니다.
// 추가적으로, "적금 계좌" 를 생성할 수 있습니다.
// 일단 지금은 이자가 없다고 가정합시다.
// 이 계좌는 메인 계좌에서 돈을 인출할 수 있으며, 메인 계좌의 돈이 없으면 인출할 수 없습니다.
// 프로그래밍 요구사항
// 적금 계좌 - 메인 계좌 간에는 트랜잭션을 신중하게 설계해야 한다.
// Transaction Isolation Level을 조사해보고, 어떤 단계를 사용해야 할지 생각해보자.
// 인당 한도는 어떻게 관리해야 할까?
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountType accountType; // MAIN, SAVING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private Long balance; // 잔액

    @Builder
    public Account(Member member, Long balance, AccountType accountType) {
        this.member = member;
        this.balance = balance;
        this.accountType = accountType;
    }
    
}

