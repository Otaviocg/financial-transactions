package com.financial.transactionconsumer.service

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong


@Service
@ConditionalOnProperty(name = ["sqs.enabled"], havingValue = "true", matchIfMissing = true)
class SqsConsumerService(
    private val processorService: TransactionProcessorService,
) {

    private val log = LoggerFactory.getLogger(SqsConsumerService::class.java)
    private val processedCount = AtomicLong(0)
    private val errorCount = AtomicLong(0)

    @SqsListener("\${sqs.queue-url}")
    fun listen(body: String) {
        try {
            processorService.process(body)

            val total = processedCount.incrementAndGet()
            if (total % 5000 == 0L) {
                log.info("Progress — processed: {}, errors: {}", total, errorCount.get())
            }
        } catch (e: Exception) {
            errorCount.incrementAndGet()
            log.error(
                "Failed to process message — it will become visible again after the visibility timeout",
                e,
            )
            throw e
        }
    }
}



