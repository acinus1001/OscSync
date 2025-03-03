package dev.kuro9.internal.smartapp.api.config

import dev.kuro9.common.network.JsonConverterFactory
import dev.kuro9.common.network.loggingOkHttpClient
import dev.kuro9.internal.smartapp.api.client.SmartAppApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.create

@Configuration
class SmartAppApiConfig {

    @Bean
    fun smartAppApi(): SmartAppApiClient = Retrofit.Builder()
        .baseUrl("https://api.smartthings.com/v1/")
        .client(loggingOkHttpClient())
        .addConverterFactory(JsonConverterFactory)
        .build()
        .create()
}