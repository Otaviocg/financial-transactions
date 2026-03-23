package com.financial.balanceapi.controller

import com.financial.balanceapi.dto.AccountBalanceResponse
import com.financial.balanceapi.dto.BalanceDto
import com.financial.balanceapi.exception.AccountNotFoundException
import com.financial.balanceapi.service.AccountService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal
import java.util.UUID
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest


@WebMvcTest(AccountController::class)
class AccountEntityControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var accountService: AccountService

    private val accountId = UUID.fromString("5b19c8b6-0cc4-4c72-a989-0c2ee15fa975")
    private val ownerId   = UUID.fromString("315e3cfe-f4af-4cd2-b298-a449e614349a")

    private fun sampleResponse() = AccountBalanceResponse(
        id       = accountId,
        owner    = ownerId,
        balance  = BalanceDto(amount = BigDecimal("183.12"), currency = "BRL"),
        updatedAt = "2025-07-05T18:04:13.433-03:00",
    )

    @Test
    fun `GET accounts-id returns 200 with the full account balance payload`() {
        given(accountService.getAccountBalance(accountId)).willReturn(sampleResponse())

        mockMvc.get("/accounts/{id}", accountId)
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id")               { value(accountId.toString()) }
                jsonPath("$.owner")            { value(ownerId.toString()) }
                jsonPath("$.balance.amount")   { value(183.12) }
                jsonPath("$.balance.currency") { value("BRL") }
                jsonPath("$.updated_at")       { value("2025-07-05T18:04:13.433-03:00") }
            }
    }

    @Test
    fun `GET accounts-id returns 404 when account does not exist`() {
        given(accountService.getAccountBalance(accountId))
            .willThrow(AccountNotFoundException(accountId))

        mockMvc.get("/accounts/{id}", accountId)
            .andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status")  { value(404) }
                jsonPath("$.error")   { value("Not Found") }
                jsonPath("$.message") { value("Account not found with id: $accountId") }
            }
    }

    @Test
    fun `GET accounts-id returns 400 when id is not a valid UUID`() {
        mockMvc.get("/accounts/not-a-valid-uuid")
            .andExpect {
                status { isBadRequest() }
            }
    }
}

