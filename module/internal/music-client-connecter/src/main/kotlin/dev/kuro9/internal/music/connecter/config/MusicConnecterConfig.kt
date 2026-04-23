package dev.kuro9.internal.music.connecter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "dev.kuro9.music-connecter")
class MusicConnecterConfig @ConstructorBinding constructor(
    val host: String,
) {
}