package com.financial.transactionconsumer.datasource.repository

import com.financial.transactionconsumer.datasource.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface TransactionRepository : JpaRepository<TransactionEntity, UUID> {

    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            INSERT INTO transactions
                (transaction_id, account_id, type, amount, currency, status, event_timestamp, created_at)
            VALUES
                (:transactionId, :accountId, :type, :amount, :currency, :status, :eventTimestamp, :createdAt)
            ON CONFLICT (transaction_id) DO NOTHING
        """
    )
    fun insertIfAbsent(
        @Param("transactionId")  transactionId: UUID,
        @Param("accountId")      accountId: UUID,
        @Param("type")           type: String,
        @Param("amount")         amount: BigDecimal,
        @Param("currency")       currency: String,
        @Param("status")         status: String,
        @Param("eventTimestamp") eventTimestamp: Long,
        @Param("createdAt")      createdAt: LocalDateTime,
    )
}

