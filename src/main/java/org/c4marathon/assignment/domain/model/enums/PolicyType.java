package org.c4marathon.assignment.domain.model.enums;

import lombok.Getter;

@Getter
public enum PolicyType {
	COMPANY_ID(-1L);

	private final Long value;

	PolicyType(Long value) {
		this.value = value;
	}
}
