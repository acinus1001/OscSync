package dev.kuro9.application.homepage.config

import kotlinx.serialization.ExperimentalSerializationApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @OptIn(ExperimentalSerializationApi::class)
    @Bean
    fun protobufHttpMessageConverter(): KotlinSerializationProtobufHttpMessageConverter {
        return KotlinSerializationProtobufHttpMessageConverter()
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.addFirst(protobufHttpMessageConverter())
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToKotlinxLocalDateConverter())
    }
}