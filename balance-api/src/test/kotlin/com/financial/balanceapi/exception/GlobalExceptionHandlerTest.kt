package com.financial.balanceapi.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleAccountNotFound returns HTTP 404 status`() {
        val response = handler.handleAccountNotFound(AccountNotFoundException(UUID.randomUUID()))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `handleAccountNotFound body has status 404, error Not Found`() {
        val response = handler.handleAccountNotFound(AccountNotFoundException(UUID.randomUUID()))

        assertNotNull(response.body)
        assertEquals(404, response.body!!.status)
        assertEquals("Not Found", response.body!!.error)
    }

    @Test
    fun `handleAccountNotFound message contains the missing account id`() {
        val id = UUID.fromString("5b19c8b6-0cc4-4c72-a989-0c2ee15fa975")
        val response = handler.handleAccountNotFound(AccountNotFoundException(id))

        assertTrue(
            response.body!!.message.contains(id.toString()),
            "Expected message to contain the account id but was: ${response.body!!.message}"
        )
    }

    @Test
    fun `handleAccountNotFound message matches AccountNotFoundException message`() {
        val id = UUID.randomUUID()
        val ex = AccountNotFoundException(id)
        val response = handler.handleAccountNotFound(ex)

        assertEquals(ex.message, response.body!!.message)
    }

    @Test
    fun `handleIllegalArgument returns HTTP 400 status`() {
        val response = handler.handleIllegalArgument(IllegalArgumentException("bad input"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `handleIllegalArgument body has status 400, error Bad Request`() {
        val response = handler.handleIllegalArgument(IllegalArgumentException("bad input"))

        assertNotNull(response.body)
        assertEquals(400, response.body!!.status)
        assertEquals("Bad Request", response.body!!.error)
    }

    @Test
    fun `handleIllegalArgument body message matches exception message`() {
        val response = handler.handleIllegalArgument(IllegalArgumentException("invalid UUID string"))

        assertEquals("invalid UUID string", response.body!!.message)
    }

    @Test
    fun `handleIllegalArgument uses fallback message when exception has no message`() {
        val response = handler.handleIllegalArgument(IllegalArgumentException())

        assertEquals("Invalid request", response.body!!.message)
    }
}

