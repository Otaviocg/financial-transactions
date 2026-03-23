package com.financial.transactionconsumer.service

import com.financial.transactionconsumer.dto.TransactionMessage
import com.financial.transactionconsumer.datasource.repository.AccountRepository
import com.financial.transactionconsumer.datasource.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class TransactionProcessorService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(TransactionProcessorService::class.java)

    @Transactional
    fun process(messageBody: String) {
        val msg: TransactionMessage = objectMapper.readValue(messageBody, TransactionMessage::class.java)
        upsertAccount(msg)
        persistTransaction(msg)
    }

    private fun persistTransaction(msg: TransactionMessage) {
        val tx = msg.transaction
        transactionRepository.insertIfAbsent(
            transactionId  = UUID.fromString(tx.id),
            accountId      = UUID.fromString(msg.account.id),
            type           = tx.type,
            amount         = tx.amount,
            currency       = tx.currency,
            status         = tx.status,
            eventTimestamp = tx.timestamp,
            createdAt      = LocalDateTime.now(ZoneOffset.UTC),
        )
    }

    private fun upsertAccount(msg: TransactionMessage) {
        val acc = msg.account
        val createdAt = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(acc.createdAt.toLong()),
            ZoneOffset.UTC,
        )
        accountRepository.upsert(
            accountId       = UUID.fromString(acc.id),
            ownerId         = UUID.fromString(acc.owner),
            status          = acc.status,
            balanceAmount   = acc.balance.amount,
            balanceCurrency = acc.balance.currency,
            updatedAt       = msg.transaction.timestamp,
            createdAt       = createdAt,
        )
    }
}

