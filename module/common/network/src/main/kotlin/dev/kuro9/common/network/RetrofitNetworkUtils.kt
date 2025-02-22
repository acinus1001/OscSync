package dev.kuro9.common.network

import dev.kuro9.common.serialization.prettyJson
import dev.kuro9.common.util.infoLog
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val JsonConverterFactory = prettyJson.asConverterFactory(MediaType.get("application/json"))

object NetworkLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestLog = """
            ----------------------------->
            ${request.method()} ${request.url()}
            
            Headers : 
            ${
            request
                .headers()
                .toMultimap()
                .entries
                .joinToString(separator = "\n") { (key, valueList) -> "$key : $valueList" }
        }
            
            Body : 
                - Content-Type : ${request.body()?.contentType() ?: "<body-is-null>"}
                - Content-Length: ${request.body()?.contentLength() ?: "<body-is-null>"}
            ${request.body() ?: "<body-is-null>"}
        """.trimIndent()

        infoLog(requestLog)

        val response = chain.proceed(request)

        val responseLog = """
            ${response.code()} ${response.message()} <--------------------
            
                        Headers : 
            ${
            response
                .headers()
                .toMultimap()
                .entries
                .joinToString(separator = "\n") { (key, valueList) -> "$key : $valueList" }
        }
        
            Body : 
                - Content-Type : ${response.body()?.contentType() ?: "<body-is-null>"}
                - Content-Length: ${response.body()?.contentLength() ?: "<body-is-null>"}
            ${response.body() ?: "<body-is-null>"}
        """.trimIndent()

        infoLog(responseLog)

        return response
    }
}