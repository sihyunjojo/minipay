package org.c4marathon.assignment.domain;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.common.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity{
    private Long id;

    private String name;

    private String email;

    private String password;

    @OneToMany(mappedBy = "Member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @Builder
    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
