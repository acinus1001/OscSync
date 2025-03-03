package dev.kuro9.application.discord.config

import dev.kuro9.multiplatform.common.serialization.prettyJson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter

@Configuration
class SerializationConfig {

    @Bean
    fun kotlinCustomJsonConverter(): KotlinSerializationJsonHttpMessageConverter {
        return KotlinSerializationJsonHttpMessageConverter(prettyJson)
    }
}