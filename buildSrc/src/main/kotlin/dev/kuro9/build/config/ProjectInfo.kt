package dev.kuro9.build.config

/**
 * need -Pproduction to use this
 *
 * ```bash
 * ./gradlew wasmJsBrowserRun -Pprod
 * ```
 */
val ProjectInfo = mapOf<String, Any>(
    "profile" to when (System.getProperty("mode")) {
        "prod" -> Profile.PRODUCTION
        else -> Profile.DEVELOPMENT
    }
).also(::println)