package org.c4marathon.assignment.transfer.validation;

import org.c4marathon.assignment.transfer.dto.TransferRequestDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentAccountsValidator implements ConstraintValidator<DifferentAccounts, TransferRequestDto> {

	@Override
	public boolean isValid(TransferRequestDto value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return !value.fromAccountId().equals(value.toAccountId());
	}
}
