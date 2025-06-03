package org.c4marathon.assignment.savingaccount.dto;

import org.c4marathon.assignment.domain.model.account.SavingAccount;

public record SavingDepositRequest(SavingAccount savingAccount, long subscribedDepositAmount) {}
