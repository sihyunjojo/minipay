package org.c4marathon.assignment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.domain.account.SavingAccount;
import org.c4marathon.assignment.domain.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private MainAccount mainAccount;

    // JPA가 관계를 매핑하면서 null이 될 수도 있기 때문에, 컬렉션은 항상 초기화해두는 것이 안전
    // final 키워드와 함께 초기화하면 해당 리스트 자체의 참조는 바뀌지 않게 돼 (내용은 변경 가능)
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private final List<SavingAccount> savingAccounts = new ArrayList<>();

    @Builder
    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
