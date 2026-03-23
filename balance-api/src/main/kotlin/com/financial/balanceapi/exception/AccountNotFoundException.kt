package com.financial.balanceapi.exception

import java.util.UUID

class AccountNotFoundException(accountId: UUID) :
    RuntimeException("Account not found with id: $accountId")

