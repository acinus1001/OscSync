package dev.kuro9.internal.smartapp.config

import dev.kuro9.common.network.JsonConverterFactory
import dev.kuro9.common.network.loggingOkHttpClient
import dev.kuro9.internal.smartapp.client.SmartAppApiClient
import dev.kuro9.internal.smartapp.model.SmartAppProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import retrofit2.Retrofit
import retrofit2.create

@Configuration
// @PropertySource("classpath:application-smartapp.properties")
class SmartAppApiConfig {


    @Bean
    fun smartAppApi(property: SmartAppProperty): SmartAppApiClient = Retrofit.Builder()
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
}