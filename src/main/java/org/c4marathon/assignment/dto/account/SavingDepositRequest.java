package org.c4marathon.assignment.dto.account;

import org.c4marathon.assignment.domain.model.account.SavingAccount;

public record SavingDepositRequest(SavingAccount savingAccount, long subscribedDepositAmount) {}
