package dev.kuro9.application.homepage.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val protoBufMessageConverter: KotlinSerializationProtobufHttpMessageConverter,
) : WebMvcConfigurer {

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.addFirst(protoBufMessageConverter)
    }
}