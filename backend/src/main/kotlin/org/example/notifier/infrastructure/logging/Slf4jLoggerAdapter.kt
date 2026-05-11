package org.example.notifier.infrastructure.logging

import org.slf4j.Logger

class Slf4jLoggerAdapter(private val logger: Logger) : LoggerPort {
    override fun info(message: String, vararg args: Any?) {
        if (args.isEmpty()) {
            logger.info(message)
        } else {
            logger.info(message, *args)
        }
    }

    override fun warn(message: String, vararg args: Any?) {
        if (args.isEmpty()) {
            logger.warn(message)
        } else {
            logger.warn(message, *args)
        }
    }

    override fun error(message: String, vararg args: Any?) {
        if (args.isEmpty()) {
            logger.error(message)
        } else {
            logger.error(message, *args)
        }
    }

    override fun error(message: String, throwable: Throwable) {
        logger.error(message, throwable)
    }

    override fun debug(message: String, vararg args: Any?) {
        if (args.isEmpty()) {
            logger.debug(message)
        } else {
            logger.debug(message, *args)
        }
    }
}
