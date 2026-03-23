package com.financial.balanceapi.datasource.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "accounts")
data class AccountEntity(

    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: UUID,

    @Column(name = "owner_id", nullable = false, updatable = false)
    val ownerId: UUID,

    @Column(name = "status", nullable = false, length = 20)
    val status: String,

    @Column(name = "balance_amount", nullable = false, precision = 19, scale = 2)
    val balanceAmount: BigDecimal,

    @Column(name = "balance_currency", nullable = false, length = 3)
    val balanceCurrency: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
)