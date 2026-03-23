package com.financial.transactionconsumer.service

import com.financial.transactionconsumer.datasource.repository.AccountRepository
import com.financial.transactionconsumer.datasource.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
class TransactionProcessorServiceTest {

    @Autowired private lateinit var processorService: TransactionProcessorService

    @MockitoBean private lateinit var accountRepository: AccountRepository
    @MockitoBean private lateinit var transactionRepository: TransactionRepository
    @MockitoBean private lateinit var sqsAsyncClient: SqsAsyncClient

    private val transactionId = UUID.randomUUID().toString()
    private val accountId     = UUID.randomUUID().toString()
    private val ownerId       = UUID.randomUUID().toString()

    private fun buildMessageJson(
        txId: String      = transactionId,
        acId: String      = accountId,
        ownId: String     = ownerId,
        type: String      = "CREDIT",
        amount: String    = "150.75",
        currency: String  = "BRL",
        status: String    = "APPROVED",
        timestamp: Long   = 1_700_000_000_000L,
        accStatus: String = "ENABLED",
        balance: String   = "150.75",
        createdAt: String = "1634874339",
    ) = """
        {
          "transaction": {
            "id":        "$txId",
            "type":      "$type",
            "amount":    $amount,
            "currency":  "$currency",
            "status":    "$status",
            "timestamp": $timestamp
          },
          "account": {
            "id":         "$acId",
            "owner":      "$ownId",
            "created_at": "$createdAt",
            "status":     "$accStatus",
            "balance": {
              "amount":   $balance,
              "currency": "$currency"
            }
          }
        }
    """.trimIndent()

    @Test
    fun `process persists transaction with correct arguments`() {
        val body = buildMessageJson()

        processorService.process(body)

        val txIdCaptor    = argumentCaptor<UUID>()
        val accIdCaptor   = argumentCaptor<UUID>()
        val typeCaptor    = argumentCaptor<String>()
        val amountCaptor  = argumentCaptor<BigDecimal>()
        val currCaptor    = argumentCaptor<String>()
        val statusCaptor  = argumentCaptor<String>()
        val tsCaptor      = argumentCaptor<Long>()

        verify(transactionRepository).insertIfAbsent(
            transactionId  = txIdCaptor.capture(),
            accountId      = accIdCaptor.capture(),
            type           = typeCaptor.capture(),
            amount         = amountCaptor.capture(),
            currency       = currCaptor.capture(),
            status         = statusCaptor.capture(),
            eventTimestamp = tsCaptor.capture(),
            createdAt      = any(),
        )

        assert(txIdCaptor.firstValue == UUID.fromString(transactionId))
        assert(accIdCaptor.firstValue == UUID.fromString(accountId))
        assert(typeCaptor.firstValue == "CREDIT")
        assert(amountCaptor.firstValue.compareTo(BigDecimal("150.75")) == 0)
        assert(currCaptor.firstValue == "BRL")
        assert(statusCaptor.firstValue == "APPROVED")
        assert(tsCaptor.firstValue == 1_700_000_000_000L)
    }

    @Test
    fun `process upserts account with correct arguments`() {
        val body = buildMessageJson()

        processorService.process(body)

        val accIdCaptor    = argumentCaptor<UUID>()
        val ownerIdCaptor  = argumentCaptor<UUID>()
        val statusCaptor   = argumentCaptor<String>()
        val balanceCaptor  = argumentCaptor<BigDecimal>()
        val currCaptor     = argumentCaptor<String>()
        val updatedCaptor  = argumentCaptor<Long>()

        verify(accountRepository).upsert(
            accountId       = accIdCaptor.capture(),
            ownerId         = ownerIdCaptor.capture(),
            status          = statusCaptor.capture(),
            balanceAmount   = balanceCaptor.capture(),
            balanceCurrency = currCaptor.capture(),
            updatedAt       = updatedCaptor.capture(),
            createdAt       = any(),
        )

        assert(accIdCaptor.firstValue == UUID.fromString(accountId))
        assert(ownerIdCaptor.firstValue == UUID.fromString(ownerId))
        assert(statusCaptor.firstValue == "ENABLED")
        assert(balanceCaptor.firstValue.compareTo(BigDecimal("150.75")) == 0)
        assert(currCaptor.firstValue == "BRL")
        assert(updatedCaptor.firstValue == 1_700_000_000_000L)
    }

    @Test
    fun `process handles DEBIT transaction`() {
        val body = buildMessageJson(type = "DEBIT", amount = "50.00", balance = "100.25")

        processorService.process(body)

        verify(transactionRepository).insertIfAbsent(
            transactionId  = any(),
            accountId      = any(),
            type           = eq("DEBIT"),
            amount         = eq(BigDecimal("50.00")),
            currency       = any(),
            status         = any(),
            eventTimestamp = any(),
            createdAt      = any(),
        )
    }

    @Test
    fun `process handles REJECTED transaction without changing balance`() {
        val body = buildMessageJson(status = "REJECTED", balance = "200.00")

        processorService.process(body)

        verify(transactionRepository).insertIfAbsent(
            transactionId  = any(),
            accountId      = any(),
            type           = any(),
            amount         = any(),
            currency       = any(),
            status         = eq("REJECTED"),
            eventTimestamp = any(),
            createdAt      = any(),
        )
        verify(accountRepository).upsert(
            accountId       = any(),
            ownerId         = any(),
            status          = any(),
            balanceAmount   = eq(BigDecimal("200.00")),
            balanceCurrency = any(),
            updatedAt       = any(),
            createdAt       = any(),
        )
    }

    @Test
    fun `process throws when message JSON is malformed`() {
        assertThrows<Exception> { processorService.process("not-json-at-all") }
    }
}

