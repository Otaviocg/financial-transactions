package com.financial.balanceapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountBalanceResponse(
    val id: UUID,
    val owner: UUID,
    val balance: BalanceDto,
    @JsonProperty(value = "updated_at")
    val updatedAt: String,
)

data class BalanceDto(
    val amount: BigDecimal,
    val currency: String,
)

