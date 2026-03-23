package com.financial.transactionconsumer.datasource.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "transactions")
data class TransactionEntity(

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    val transactionId: UUID,

    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: UUID,

    @Column(name = "type", nullable = false, length = 10)
    val type: String,

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Column(name = "status", nullable = false, length = 15)
    val status: String,

    @Column(name = "event_timestamp", nullable = false)
    val eventTimestamp: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
)