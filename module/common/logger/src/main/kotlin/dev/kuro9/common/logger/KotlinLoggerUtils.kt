@file:Suppress("UNUSED")

package dev.kuro9.common.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun <reified T> T.debugLog(message: String) = logger().debug(message)
inline fun <reified T> T.debugLog(message: String, vararg params: Any?) = logger().debug(message, *params)
inline fun <reified T> T.debugLog(message: String, t: Throwable) = logger().debug(message, t)

inline fun <reified T> T.infoLog(message: String) = logger().info(message)
inline fun <reified T> T.infoLog(message: String, vararg params: Any?) = logger().info(message, *params)
inline fun <reified T> T.infoLog(message: String, t: Throwable) = logger().info(message, t)

inline fun <reified T> T.warnLog(message: String) = logger().warn(message)
inline fun <reified T> T.warnLog(message: String, vararg params: Any?) = logger().warn(message, *params)
inline fun <reified T> T.warnLog(message: String, t: Throwable) = logger().warn(message, t)

inline fun <reified T> T.errorLog(message: String) = logger().error(message)
inline fun <reified T> T.errorLog(message: String, vararg params: Any?) = logger().error(message, *params)
inline fun <reified T> T.errorLog(message: String, t: Throwable) = logger().error(message, t)

class DelegateLogger<T : Any> : ReadOnlyProperty<T, Logger> {
    private var logger: Logger? = null
    override fun getValue(thisRef: T, property: KProperty<*>): Logger {
        return logger ?: LoggerFactory.getLogger(thisRef::class.java).also { this.logger = it }
    }
}

inline fun <reified T : Any> T.useLogger(): DelegateLogger<T> = DelegateLogger()