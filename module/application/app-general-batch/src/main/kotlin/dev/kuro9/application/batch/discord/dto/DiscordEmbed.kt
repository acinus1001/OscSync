package dev.kuro9.application.batch.discord

import dev.kuro9.application.batch.discord.DiscordEmbed.Field
import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmbed(
    val title: String,
    val description: String,
    val color: Int = 0x000000,
    val fields: List<Field> = emptyList(),
) {

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
    private val fields: MutableList<Field> = mutableListOf()

    fun field(action: FieldBuilder.() -> Unit) {
        fields.add(FieldBuilder().apply(action).build())
    }

    fun build() = DiscordEmbed(
        title = title!!,
        description = description!!,
        color = color,
        fields = fields,
    )

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