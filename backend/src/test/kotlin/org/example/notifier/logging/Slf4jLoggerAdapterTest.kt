package org.example.notifier.logging

import org.example.notifier.infrastructure.logging.Slf4jLoggerAdapter
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.slf4j.Logger

class Slf4jLoggerAdapterTest {

    @Test
    fun `should delegate info to slf4j logger`() {
        val mockLogger = mock(Logger::class.java)
        val adapter = Slf4jLoggerAdapter(mockLogger)
        val message = "test message"

        adapter.info(message)

        verify(mockLogger).info(message)
    }

    @Test
    fun `should delegate error to slf4j logger`() {
        val mockLogger = mock(Logger::class.java)
        val adapter = Slf4jLoggerAdapter(mockLogger)
        val message = "error message"
        val exception = RuntimeException("oops")

        adapter.error(message, exception)

        verify(mockLogger).error(message, exception)
    }
}
