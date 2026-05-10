package dev.kuro9.domain.smartapp.webhook.enums

import com.smartthings.sdk.smartapp.core.models.DeviceSetting
import com.smartthings.sdk.smartapp.core.models.Section
import com.smartthings.sdk.smartapp.core.models.SettingType

enum class InternalDeviceType(
    val internalId: String,
    val deviceName: String,
    val description: String,
    val required: Boolean,
    val multiple: Boolean,
    val capabilities: List<String>,
    val permissions: List<DeviceSetting.PermissionsEnum>,
) {
    MAIN_LIGHT(
        "main-light-20240414",
        "Main Light",
        "Main Light in room",
        true,
        false,
        listOf("switch"),
        listOf(DeviceSetting.PermissionsEnum.R, DeviceSetting.PermissionsEnum.W)
    ),
    SUB_LIGHT(
        "sub-light-20240414",
        "Sub Light",
        "Sub Light in room",
        false,
        false,
        listOf("switch"),
        listOf(DeviceSetting.PermissionsEnum.R, DeviceSetting.PermissionsEnum.W)
    );
//    HEATER(
//        "heater-20240414",
//        "Heater",
//        "Heater in room",
//        false,
//        false,
//        listOf("level"),
//        listOf(DeviceSetting.PermissionsEnum.R, DeviceSetting.PermissionsEnum.W, DeviceSetting.PermissionsEnum.X)
//    );


    fun toSection() = Section().apply {
        name = this@InternalDeviceType.name
        settings = listOf(
            DeviceSetting().apply {
                id = this@InternalDeviceType.internalId
                name = this@InternalDeviceType.deviceName
                type = SettingType.DEVICE
                description = this@InternalDeviceType.description
                isRequired = this@InternalDeviceType.required
                isMultiple = this@InternalDeviceType.multiple
                capabilities = this@InternalDeviceType.capabilities
                permissions = this@InternalDeviceType.permissions
            }
        )
    }
}