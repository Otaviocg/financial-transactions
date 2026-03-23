package com.financial.balanceapi.controller

import com.financial.balanceapi.dto.AccountBalanceResponse
import com.financial.balanceapi.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    @GetMapping("/{id}")
    fun getBalance(@PathVariable id: UUID): ResponseEntity<AccountBalanceResponse> =
        ResponseEntity.ok(accountService.getAccountBalance(id))
}

