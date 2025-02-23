package dev.kuro9.internal.smartapp.config

import dev.kuro9.common.network.JsonConverterFactory
import dev.kuro9.common.network.loggingOkHttpClient
import dev.kuro9.internal.smartapp.client.SmartAppApiClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpHeaders
import retrofit2.Retrofit
import retrofit2.create

@Configuration
@PropertySource("classpath:application-smartapp.properties")
class SmartAppApiConfig {

    @Bean
    fun smartAppProperty(
        @Value("\${dev.kuro9.internal.smartapp.token}") token: String,
    ) = Property(token = token)


    @Bean
    fun smartAppApi(property: Property): SmartAppApiClient = Retrofit.Builder()
        .baseUrl("https://api.smartthings.com/v1/")
        .client(loggingOkHttpClient {
            addNetworkInterceptor { chain ->
                chain.request().newBuilder()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${property.token}")
                    .build()
                    .run(chain::proceed)
            }
        })
        .addConverterFactory(JsonConverterFactory)
        .build()
        .create()


    data class Property(val token: String)
}