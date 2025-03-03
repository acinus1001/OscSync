package dev.kuro9.internal.smartapp.webhook.model

import dev.kuro9.internal.smartapp.model.DeviceCapability
import dev.kuro9.internal.smartapp.model.Permission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * [DOCS](https://developer.smartthings.com/docs/connected-services/configuration)
 */
@Serializable
@ConsistentCopyVisibility
data class SmartAppWebhookResponse private constructor(
    val configurationData: ConfigurationField,
) {

    constructor(initialize: ConfigurationData.InitData) : this(
        ConfigurationField(initialize = initialize)
    )

    constructor(page: ConfigurationData.PageData) : this(
        ConfigurationField(page = page)
    )

    @Serializable
    data class ConfigurationField(
        val initialize: ConfigurationData.InitData? = null,
        val page: ConfigurationData.PageData? = null,
    )

    @Serializable
    sealed interface ConfigurationData {

        @Serializable
        data class InitData(
            val name: String,
            val description: String,
            val id: String,
            val permission: List<String>,
            val firstPageId: String,
        ) : ConfigurationData

        @Serializable
        data class PageData(
            val pageId: String,
            val nextPageId: String?,
            val previousPageId: String?,
            val complete: Boolean,
            val name: String,
            val settings: List<Setting>
        ) {

            @Serializable
            sealed interface Setting {
                val id: String
                val name: String
                val description: String

                @[SerialName("DEVICE") Serializable]
                data class DeviceType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val required: Boolean,
                    val multiple: Boolean,
                    val capabilities: List<DeviceCapability>,
                    val permission: List<Permission>,
                ) : Setting

                @[SerialName("TEXT") Serializable]
                data class TextType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val required: Boolean,
                    val defaultValue: String?
                ) : Setting

                @[SerialName("BOOLEAN") Serializable]
                data class BooleanType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val required: Boolean,
                    @Transient
                    private val defaultValue: Boolean? = null,
                ) : Setting {

                    @SerialName("defaultValue")
                    val defaultValueStr: String? = defaultValue?.toString()
                }

                @ConsistentCopyVisibility
                @[SerialName("ENUM") Serializable]
                data class EnumType private constructor(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val required: Boolean,
                    val multiple: Boolean,
                    val options: List<Option>?,
                    val groupedOptions: List<GroupedOption>?,
                ) : Setting {

                    companion object {
                        fun withOptions(
                            id: String,
                            name: String,
                            description: String,
                            required: Boolean,
                            multiple: Boolean,
                            options: List<Option>,
                        ) = EnumType(
                            id = id,
                            name = name,
                            description = description,
                            required = required,
                            multiple = multiple,
                            options = options,
                            groupedOptions = null,
                        )

                        fun withGroupOptions(
                            id: String,
                            name: String,
                            description: String,
                            required: Boolean,
                            multiple: Boolean,
                            groupedOptions: List<GroupedOption>,
                        ) = EnumType(
                            id = id,
                            name = name,
                            description = description,
                            required = required,
                            multiple = multiple,
                            options = null,
                            groupedOptions = groupedOptions,
                        )
                    }

                    @Serializable
                    data class GroupedOption(
                        val name: String,
                        val options: List<Option>,
                    )

                    @Serializable
                    data class Option(
                        val id: String,
                        val name: String,
                    )
                }

                @[SerialName("LINK") Serializable]
                data class LinkType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val url: String,
                    val image: String,
                ) : Setting

                @[SerialName("PAGE") Serializable]
                data class PageType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val page: String,
                    val image: String,
                ) : Setting

                @[SerialName("IMAGE") Serializable]
                data class ImageType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val image: String,
                ) : Setting

                @[SerialName("ICON") Serializable]
                data class IconType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val image: String,
                ) : Setting

                @[SerialName("TIME") Serializable]
                data class TimeType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                ) : Setting

                @[SerialName("PARAGRAPH") Serializable]
                data class ParagraphType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val defaultValue: String,
                ) : Setting

                @[SerialName("EMAIL") Serializable]
                data class EmailType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                ) : Setting

                @[SerialName("DECIMAL") Serializable]
                data class DecimalType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                ) : Setting

                @[SerialName("NUMBER") Serializable]
                data class NumberType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                ) : Setting

                @[SerialName("PHONE") Serializable]
                data class PhoneType(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                ) : Setting

                @[SerialName("OAUTH") Serializable]
                data class Type(
                    override val id: String,
                    override val name: String,
                    override val description: String,
                    val browser: Boolean,
                    val urlTemplate: String,
                ) : Setting

            }
        }

    }
}