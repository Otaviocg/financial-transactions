package com.financial.balanceapi.service

import com.financial.balanceapi.dto.AccountBalanceResponse
import com.financial.balanceapi.dto.BalanceDto
import com.financial.balanceapi.exception.AccountNotFoundException
import com.financial.balanceapi.datasource.entity.AccountEntity
import com.financial.balanceapi.datasource.repository.AccountRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class AccountService(private val accountRepository: AccountRepository) {

    companion object {

        private val FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

        private val ZONE: ZoneId = ZoneId.of("America/Sao_Paulo")
    }

    fun getAccountBalance(accountId: UUID): AccountBalanceResponse =
        accountRepository
            .findById(accountId)
            .orElseThrow { AccountNotFoundException(accountId) }
            .toResponse()

    private fun AccountEntity.toResponse(): AccountBalanceResponse {
        val formattedTimestamp = Instant
            .ofEpochSecond(
                updatedAt / 1_000_000L,
                (updatedAt % 1_000_000L) * 1_000L
            )
            .atZone(ZONE)
            .format(FORMATTER)

        return AccountBalanceResponse(
            id = accountId,
            owner = ownerId,
            balance = BalanceDto(
                amount = balanceAmount,
                currency = balanceCurrency,
            ),
            updatedAt = formattedTimestamp,
        )
    }
}

