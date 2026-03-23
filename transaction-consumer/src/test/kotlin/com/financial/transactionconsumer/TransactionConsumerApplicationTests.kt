package com.financial.transactionconsumer

import com.financial.transactionconsumer.datasource.repository.AccountRepository
import com.financial.transactionconsumer.datasource.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient

@SpringBootTest
class TransactionConsumerApplicationTests {

    // Infrastructure beans that would otherwise require external services
    @MockitoBean private lateinit var sqsAsyncClient: SqsAsyncClient
    @MockitoBean private lateinit var accountRepository: AccountRepository
    @MockitoBean private lateinit var transactionRepository: TransactionRepository

    @Test
    fun contextLoads() {
        // Verifies that the Spring application context starts up without errors.
    }
}

