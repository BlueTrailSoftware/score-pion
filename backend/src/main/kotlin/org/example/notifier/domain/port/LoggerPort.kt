package org.example.notifier.infrastructure.logging

interface LoggerPort {
    fun info(message: String, vararg args: Any?)
    fun warn(message: String, vararg args: Any?)
    fun error(message: String, vararg args: Any?)
    fun error(message: String, throwable: Throwable)
    fun debug(message: String, vararg args: Any?)
}
