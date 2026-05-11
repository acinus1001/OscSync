package dev.kuro9.application.homepage.iot.controller

import dev.kuro9.application.homepage.utils.ROOT_USER_ID
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.multiplatform.common.types.app.homepage.iot.DeviceSwitchRequest
import dev.kuro9.multiplatform.common.types.smartthings.SmartAppUserDevice
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/services/iot")
class IotController(
    private val iotService: SmartAppUserService
) {

    @GetMapping("/root/devices")
    fun getRootDevices(@AuthenticationPrincipal user: DiscordUserDetail): List<SmartAppUserDevice> {
        return iotService.getRegisteredDevices(ROOT_USER_ID)
    }

    @PutMapping("/root/devices/{deviceId}/switch")
    fun switchRootDevice(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable deviceId: String,
        @RequestBody body: DeviceSwitchRequest,
    ): ResponseEntity<Nothing> {
        val isSucceeded = runBlocking {
            iotService.executeDevice(
                userId = ROOT_USER_ID,
                deviceId = deviceId,
                deviceComponentId = "main",
                deviceCapabilityId = "switch",
                desireState = body.desireState
            )
        }

        return ResponseEntity(if (isSucceeded) HttpStatus.NO_CONTENT else HttpStatus.INTERNAL_SERVER_ERROR)
    }
}