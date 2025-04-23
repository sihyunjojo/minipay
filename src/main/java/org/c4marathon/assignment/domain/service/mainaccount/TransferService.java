package org.c4marathon.assignment.domain.service.mainaccount;

import org.c4marathon.assignment.domain.validator.MainAccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferService {

	private final MainAccountValidator mainAccountValidator;
	private final TransferRetryService transferRetryService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void transfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		mainAccountValidator.validateTransfer(fromAccountId, toAccountId, transferAmount);
	}
}
