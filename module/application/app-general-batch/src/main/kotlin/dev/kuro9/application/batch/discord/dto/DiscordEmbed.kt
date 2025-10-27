package dev.kuro9.application.batch.discord.dto

import dev.kuro9.application.batch.discord.dto.DiscordEmbed.Field
import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmbed(
    val title: String,
    val description: String,
    val color: Int = 0x000000,
    val fields: List<Field> = emptyList(),
    val image: Image? = null,
) {
    @Serializable
    data class Image(val url: String)

    @Serializable
    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean = true,
    )
}

@DiscordEmbedDsl
class DiscordEmbedBuilder {
    var title: String? = null
    var description: String? = null
    var color: Int = 0x000000
    var image: String? = null
    private val fields: MutableList<Field> = mutableListOf()

    @Suppress("FunctionName")
    fun Field(action: FieldBuilder.() -> Unit) {
        fields.add(FieldBuilder().apply(action).build())
    }

    internal fun build() = DiscordEmbed(
        title = title!!,
        description = description!!,
        color = color,
        fields = fields,
        image = image?.let { DiscordEmbed.Image(it) }
    )

    @DiscordEmbedDsl
    class FieldBuilder {
        var name: String? = null
        var value: String? = null
        var inline: Boolean = true
        fun build(): Field = Field(name!!, value!!, inline)
    }
}

@Suppress("FunctionName")
fun Embed(action: DiscordEmbedBuilder.() -> Unit): DiscordEmbed {
    return DiscordEmbedBuilder().apply(action).build()
}

@DslMarker
annotation class DiscordEmbedDsl