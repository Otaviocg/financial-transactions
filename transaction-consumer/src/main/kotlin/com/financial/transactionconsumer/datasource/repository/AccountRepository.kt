package com.financial.transactionconsumer.datasource.repository

import com.financial.transactionconsumer.datasource.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface AccountRepository : JpaRepository<AccountEntity, UUID> {

    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            INSERT INTO accounts
                (account_id, owner_id, status, balance_amount, balance_currency, updated_at, created_at)
            VALUES
                (:accountId, :ownerId, :status, :balanceAmount, :balanceCurrency, :updatedAt, :createdAt)
            ON CONFLICT (account_id) DO UPDATE SET
                status           = EXCLUDED.status,
                balance_amount   = EXCLUDED.balance_amount,
                balance_currency = EXCLUDED.balance_currency,
                updated_at       = EXCLUDED.updated_at
            WHERE accounts.updated_at < EXCLUDED.updated_at
        """
    )
    fun upsert(
        @Param("accountId")       accountId: UUID,
        @Param("ownerId")         ownerId: UUID,
        @Param("status")          status: String,
        @Param("balanceAmount")   balanceAmount: BigDecimal,
        @Param("balanceCurrency") balanceCurrency: String,
        @Param("updatedAt")       updatedAt: Long,
        @Param("createdAt")       createdAt: LocalDateTime,
    )
}

