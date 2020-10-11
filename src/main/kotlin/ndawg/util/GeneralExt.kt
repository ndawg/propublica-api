package ndawg.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mu.KLogger
import mu.KotlinLogging
import kotlin.reflect.KClass

/**
 * Uses a reified type to produce a type from a JSON string
 */
inline fun <reified T : Any> Gson.from(string: String): T {
    return requireNotNull(this.fromJson(string, object: TypeToken<T?>() {}.type)) {
        "Failed to produce ${T::class} from: $string" + if (string.isEmpty()) "[empty]" else ""
    }
}

/**
 * Uses a reified type to produce a type from a JSON string
 */
inline fun <reified T : Any?> Gson.get(string: String?): T? {
    if (string == null) return null
    return this.fromJson(string, object: TypeToken<T?>() {}.type)
}

private val loggers = mutableMapOf<Class<*>, KLogger>()

/**
 * Obtains a logger instance for the given class
 */
fun log(clazz: Class<*>): KLogger {
    return loggers.computeIfAbsent(clazz) {
        val name = clazz.name
        val slicedName = when {
            name.contains("Kt$") -> name.substringBefore("Kt$")
            name.contains("$") -> name.substringBefore("$")
            else -> name
        }
        KotlinLogging.logger(slicedName)
    }
}

/**
 * Obtains a logger instance for the given class
 */
fun log(kClass: KClass<*>): KLogger {
    return log(kClass.java)
}

/**
 * Obtains a logger instance for the current class
 */
fun Any.log(): KLogger = log(this::class.java)
