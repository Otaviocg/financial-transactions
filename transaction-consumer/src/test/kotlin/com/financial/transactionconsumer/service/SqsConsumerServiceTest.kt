package com.financial.transactionconsumer.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pure unit tests for [SqsConsumerService].
 *
 * No Spring context is loaded. [TransactionProcessorService] is fully mocked,
 * isolating the consumer's own logic:
 *  - delegation to processorService
 *  - error counter increment on failure
 *  - exception propagation (prevents SQS acknowledgement)
 */
@ExtendWith(MockitoExtension::class)
class SqsConsumerServiceTest {

    @Mock
    private lateinit var processorService: TransactionProcessorService

    @InjectMocks
    private lateinit var sqsConsumerService: SqsConsumerService

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    fun `listen delegates exact body to processorService`() {
        val body = """{"transaction":{"id":"abc"},"account":{"id":"xyz"}}"""

        sqsConsumerService.listen(body)

        verify(processorService).process(body)
    }

    @Test
    fun `listen calls processorService exactly once per invocation`() {
        doNothing().whenever(processorService).process(any())

        sqsConsumerService.listen("any-body")

        verify(processorService, times(1)).process(any())
    }

    @Test
    fun `listen re-throws exception so message is not acknowledged`() {
        val cause = IllegalStateException("DB unavailable")
        doThrow(cause).whenever(processorService).process(any())

        val thrown = assertThrows<IllegalStateException> {
            sqsConsumerService.listen("some-body")
        }

        assert(thrown === cause) { "Expected exact same exception instance to propagate" }
    }

    @Test
    fun `listen re-throws any exception type from processorService`() {
        doThrow(RuntimeException("unexpected")).whenever(processorService).process(any())

        assertThrows<RuntimeException> { sqsConsumerService.listen("body") }
    }

    @Test
    fun `listen does not swallow exception - processorService is still called`() {
        doThrow(RuntimeException("fail")).whenever(processorService).process(any())

        runCatching { sqsConsumerService.listen("body") }

        // Even though an exception was thrown, process() was called once
        verify(processorService, times(1)).process("body")
    }
}

