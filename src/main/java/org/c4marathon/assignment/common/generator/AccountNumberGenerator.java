package org.c4marathon.assignment.common.generator;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.model.account.enums.AccountType;
import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

	private static final int ACCOUNT_NUMBER_LENGTH = 12;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final AccountPolicyProperties policy;

	public String generate(AccountType accountType) {
		String raw = generateRawNumber();
		String prefix = getPrefix(accountType);
		return format(prefix, raw);
	}

	private String generateRawNumber() {
		StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
		for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
			sb.append(RANDOM.nextInt(10)); // 0~9
		}
		return sb.toString();
	}

	private String getPrefix(AccountType accountType) {
		return switch (accountType) {
			case MAIN_ACCOUNT -> policy.getMain().getAccountPrefix();
			case SAVING_ACCOUNT -> policy.getSaving().getAccountPrefix();
			case EXTERNAL_ACCOUNT -> throw new UnsupportedOperationException("외부 계좌는 prefix가 없습니다.");
		};
	}

	private String format(String prefix, String rawNumber) {
		if (rawNumber.length() != 12) {
			throw new IllegalArgumentException("계좌번호는 12자리여야 합니다.");
		}
		return String.format("%s-%s-%s", prefix,
			rawNumber.substring(0, 2),
			rawNumber.substring(2));
	}
}
