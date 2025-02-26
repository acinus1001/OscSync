package dev.kuro9.common.network

import dev.kuro9.common.logger.errorLog
import dev.kuro9.common.logger.infoLog
import dev.kuro9.multiplatform.common.serialization.prettyJson
import okhttp3.*
import okio.Buffer
import okio.GzipSource
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.time.measureTimedValue

val JsonConverterFactory = prettyJson.asConverterFactory(MediaType.get("application/json"))

fun loggingOkHttpClient(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach { addInterceptor(it) }
        addNetworkInterceptor(NetworkLogger)
    }
    .build()

fun loggingOkHttpClient(builderAction: OkHttpClient.Builder.() -> Unit): OkHttpClient {
    return OkHttpClient.Builder()
        .apply(builderAction)
        .addNetworkInterceptor(NetworkLogger)
        .build()
}

object NetworkLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestLog = """
---> ${request.method()} ${request.url()}

Headers : 
${
            request
                .headers()
                .toMultimap()
                .entries
                .joinToString(separator = "\n") { (key, valueList) -> "\t$key : $valueList" }
        }

Body : 
    Content-Type : ${request.body()?.contentType() ?: "<body-is-null>"}
    Content-Length: ${request.body()?.contentLength() ?: "<body-is-null>"}
${request.body()?.toStringBody() ?: "<body-is-null>"}
"""

        infoLog(requestLog)

        val (response, timeValue) = measureTimedValue { chain.proceed(request) }

        val source = response.body()?.source()
        source?.request(Long.MAX_VALUE)

        val charset = response.body()?.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8

        val responseBody = when {
            source == null -> null
            response.header("Content-Encoding") == "gzip" -> {
                GzipSource(source.buffer.clone()).use { gzippedResponseBody ->
                    Buffer().let { buffer ->
                        buffer.writeAll(gzippedResponseBody)
                        buffer.readString(charset)
                    }
                }
            }

            else -> source.buffer.clone().readString(charset)
        }

        val responseLog = """
<--- ${response.code()} ${response.message()} (${timeValue})

Headers : 
${
            response
                .headers()
                .toMultimap()
                .entries
                .joinToString(separator = "\n") { (key, valueList) -> "\t$key : $valueList" }
        }

Body : 
    Content-Type : ${response.body()?.contentType() ?: "<body-is-null>"}
    Content-Length: ${response.body()?.contentLength() ?: "<body-is-null>"}
${responseBody ?: "<body-is-null>"}
"""

        infoLog(responseLog)

        return response
    }
}

fun RequestBody.toStringBody(): String {
    return runCatching {
        Buffer().apply {
            this@toStringBody.writeTo(this)
        }.readUtf8()
    }.getOrElse {
        errorLog("error while read requestBody", it)
        "<body-read-err>"
    }
}