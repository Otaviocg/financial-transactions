package com.financial.transactionconsumer.dto

import java.math.BigDecimal

data class TransactionMessage(
    val transaction: TransactionPayload,
    val account: AccountPayload,
)

data class TransactionPayload(
    val id: String,
    val type: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String,
    val timestamp: Long,
)

data class AccountPayload(
    val id: String,
    val owner: String,
    val createdAt: String,
    val status: String,
    val balance: BalancePayload,
)

data class BalancePayload(
    val amount: BigDecimal,
    val currency: String,
)

