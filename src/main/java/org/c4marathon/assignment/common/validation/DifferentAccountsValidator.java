package org.c4marathon.assignment.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.c4marathon.assignment.dto.transfer.TransferRequestDto;

public class DifferentAccountsValidator implements ConstraintValidator<DifferentAccounts, TransferRequestDto> {

	@Override
	public boolean isValid(TransferRequestDto value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return !value.fromAccountId().equals(value.toAccountId());
	}
}
