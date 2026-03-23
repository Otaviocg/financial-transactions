package com.financial.transactionconsumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TransactionConsumerApplication

fun main(args: Array<String>) {
	runApplication<TransactionConsumerApplication>(*args)
}
