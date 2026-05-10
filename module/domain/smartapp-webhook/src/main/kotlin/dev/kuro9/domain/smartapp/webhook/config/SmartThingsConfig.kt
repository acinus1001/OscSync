package dev.kuro9.domain.smartapp.webhook.config

import com.smartthings.sdk.client.ApiClient
import com.smartthings.sdk.smartapp.core.SmartAppDefinition
import com.smartthings.sdk.smartapp.core.extensions.HttpVerificationService
import com.smartthings.sdk.smartapp.core.models.ConfigurationResponseData
import com.smartthings.sdk.smartapp.core.models.InitializeSetting
import com.smartthings.sdk.smartapp.core.models.Page
import com.smartthings.sdk.smartapp.core.service.TokenRefreshService
import com.smartthings.sdk.smartapp.core.service.TokenRefreshServiceImpl
import com.smartthings.sdk.smartapp.spring.SpringSmartAppDefinition
import dev.kuro9.domain.smartapp.webhook.enums.InternalDeviceType
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SmartThingsConfig {
    @Bean
    fun httpClient(): CloseableHttpClient {
        return HttpClients.createDefault()
    }

    @Bean
    fun apiClient(): ApiClient {
        return ApiClient()
    }

    @Bean
    fun httpVerificationService(): HttpVerificationService {
        return HttpVerificationService(httpClient())
    }

    @Bean
    fun tokenRefreshService(
        config: SmartAppConfigProperties
    ): TokenRefreshService {
        return TokenRefreshServiceImpl(
            config.smartAppClientId,
            config.smartAppClientSecret,
            httpClient()
        )
    }


    @Bean
    fun smartAppDef(applicationContext: ApplicationContext): SmartAppDefinition =
        SpringSmartAppDefinition.of(applicationContext)

    @Bean(name = ["initializeResponse"])
    fun initializeResponse(): ConfigurationResponseData {
        return ConfigurationResponseData().apply {
            initialize = InitializeSetting().apply {
                name = "SmartThingsOsc"
                description = "SmartThings custom Controller"
                id = "app"
                permissions = listOf("r:devices:*", "w:devices:*")
                firstPageId = "1"
            }
        }
    }

    @Bean(name = ["pageResponse"])
    fun pageResponse(): ConfigurationResponseData {
        return ConfigurationResponseData().apply {
            page = Page().apply {
                pageId = "1"
                name = "SmartThingsOsc"
                nextPageId = null
                previousPageId = null
                isComplete = true
                sections = InternalDeviceType.entries.map { it.toSection() }
            }
        }
    }
}
