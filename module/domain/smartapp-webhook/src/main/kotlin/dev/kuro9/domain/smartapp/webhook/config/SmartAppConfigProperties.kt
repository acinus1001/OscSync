package dev.kuro9.domain.smartapp.webhook.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("dev.kuro9.smartapp")
data class SmartAppConfigProperties @ConstructorBinding constructor(
//    val smartThingToken: String,
//    val smartThingBaseUrl: String,

    val smartAppClientId: String,
    val smartAppClientSecret: String
)
