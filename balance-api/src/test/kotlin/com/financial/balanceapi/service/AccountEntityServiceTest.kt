package com.financial.balanceapi.service

import com.financial.balanceapi.exception.AccountNotFoundException
import com.financial.balanceapi.datasource.entity.AccountEntity
import com.financial.balanceapi.datasource.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AccountEntityServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @InjectMocks
    private lateinit var accountService: AccountService

    private val accountId = UUID.fromString("5b19c8b6-0cc4-4c72-a989-0c2ee15fa975")
    private val ownerId   = UUID.fromString("315e3cfe-f4af-4cd2-b298-a449e614349a")

    private fun buildAccount(updatedAt: Long = 1_751_641_364_589_998L) = AccountEntity(
        accountId       = accountId,
        ownerId         = ownerId,
        status          = "ENABLED",
        balanceAmount   = BigDecimal("183.12"),
        balanceCurrency = "BRL",
        updatedAt       = updatedAt,
        createdAt       = LocalDateTime.now(ZoneOffset.UTC),
    )

    @Test
    fun `getAccountBalance maps account id and owner correctly`() {
        given(accountRepository.findById(accountId)).willReturn(Optional.of(buildAccount()))

        val response = accountService.getAccountBalance(accountId)

        assertEquals(accountId, response.id)
        assertEquals(ownerId, response.owner)
    }

    @Test
    fun `getAccountBalance maps balance amount and currency correctly`() {
        given(accountRepository.findById(accountId)).willReturn(Optional.of(buildAccount()))

        val response = accountService.getAccountBalance(accountId)

        assertEquals(BigDecimal("183.12"), response.balance.amount)
        assertEquals("BRL", response.balance.currency)
    }

    @Test
    fun `getAccountBalance converts microseconds to the correct timestamp`() {
        val updatedAt = 1751641364589998L
        given(accountRepository.findById(accountId)).willReturn(Optional.of(buildAccount(updatedAt)))

        val response = accountService.getAccountBalance(accountId)

        val expected = Instant
            .ofEpochSecond(updatedAt / 1_000_000L, (updatedAt % 1_000_000L) * 1_000L)
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))

        assertEquals(expected, response.updatedAt)
    }

    @Test
    fun `getAccountBalance throws AccountNotFoundException when account does not exist`() {
        given(accountRepository.findById(accountId)).willReturn(Optional.empty())

        assertThrows<AccountNotFoundException> {
            accountService.getAccountBalance(accountId)
        }
    }

    @Test
    fun `getAccountBalance exception message contains the missing account id`() {
        given(accountRepository.findById(accountId)).willReturn(Optional.empty())

        val ex = assertThrows<AccountNotFoundException> {
            accountService.getAccountBalance(accountId)
        }

        assertTrue(
            ex.message!!.contains(accountId.toString()),
            "Expected exception message to contain id but was: ${ex.message}"
        )
    }

    @Test
    fun `getAccountBalance delegates to repository with the exact given id`() {
        given(accountRepository.findById(accountId)).willReturn(Optional.of(buildAccount()))

        accountService.getAccountBalance(accountId)

        verify(accountRepository).findById(accountId)
    }
}

