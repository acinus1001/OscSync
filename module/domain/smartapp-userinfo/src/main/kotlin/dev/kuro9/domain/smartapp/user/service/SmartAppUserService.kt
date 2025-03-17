package dev.kuro9.domain.smartapp.user.service

import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.DuplicatedRegisterException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.NotSupportException
import dev.kuro9.domain.smartapp.user.exception.SmartAppUserException.CredentialNotFoundException
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserDeviceEntity
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserDevices
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponseObject.DeviceInfo
import dev.kuro9.internal.smartapp.api.exception.ApiNotSuccessException
import dev.kuro9.internal.smartapp.api.service.SmartAppApiService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SmartAppUserService(
    private val userCredentialService: SmartAppCredentialService,
    private val apiClient: SmartAppApiService,
    private val database: Database,
) {

    @Throws(CredentialNotFoundException::class)
    suspend fun getUserDevices(userId: Long): SmartAppDeviceListResponse {
        return apiClient.listDevices(userCredentialService.getUserCredential(userId))
    }

    @Throws(CredentialNotFoundException::class)
    suspend fun getUserDevice(userId: Long, deviceId: String): DeviceInfo {
        return apiClient.getDeviceInfo(
            smartAppToken = userCredentialService.getUserCredential(userId),
            deviceId = deviceId,
        )
    }

    @Transactional
    fun registerDevice(
        userId: Long,
        deviceId: String,
        deviceComponentId: String,
        deviceCapabilityId: String,
        deviceName: String,
    ) {
        transaction(database) {
            SmartAppUserDevices.upsert {
                it[SmartAppUserDevices.userId] = userId
                it[SmartAppUserDevices.deviceId] = deviceId
                it[SmartAppUserDevices.deviceComponent] = deviceComponentId
                it[SmartAppUserDevices.deviceCapability] = deviceCapabilityId
                it[SmartAppUserDevices.deviceName] = deviceName
            }
        }
    }

    /**
     * @return 등록한 device name
     */
    @Throws(
        CredentialNotFoundException::class,
        DuplicatedRegisterException::class,
        NotSupportException::class,
        SmartAppDeviceException.NotFoundException::class,
    )
    suspend fun registerDeviceWithId(userId: Long, deviceId: String, deviceName: String?): String {
        val deviceInfo = runCatching { getUserDevice(userId, deviceId) }
            .getOrElse { e ->
                // 403 handle (if not exist)
                // 400 handle (if deviceId is malformed)

                if (e !is ApiNotSuccessException) throw e
                throw SmartAppDeviceException.NotFoundException("Device with id $deviceId not found.")
            }

        val component = deviceInfo.components.firstOrNull { it.id == "main" }
            ?: throw NotSupportException("support only main component, but main not found.")

        val capability = component.capabilities.firstOrNull { it.id == "switch" }
            ?: throw NotSupportException("support only switch capability, but switch not found.")

        val deviceName = deviceName ?: deviceInfo.name ?: deviceId

        registerDevice(
            userId = userId,
            deviceId = deviceId,
            deviceComponentId = component.id,
            deviceCapabilityId = capability.id,
            deviceName = deviceName
        )

        return deviceName
    }

    suspend fun executeDevice(
        userId: Long,
        deviceId: String,
        deviceComponentId: String,
        deviceCapabilityId: String,
        desireState: Boolean,
    ): Boolean {
        check(deviceComponentId == "main") { "support only main component, but main not found." }
        check(deviceCapabilityId == "switch") { "support only switch capability, but switch not found." }

        val result = runCatching {
            apiClient.executeDeviceCommand(
                smartAppToken = userCredentialService.getUserCredential(userId),
                deviceId = deviceId,
                request = SmartAppDeviceCommandRequest(
                    SmartAppDeviceCommandRequest.Command(
                        component = deviceComponentId,
                        capability = deviceCapabilityId,
                        command = if (desireState) "on" else "off",
                        arguments = emptyList()
                    )
                )
            )
        }.getOrElse { e ->
            if (e !is ApiNotSuccessException) throw e
            throw NotSupportException()
        }

        return result.results.singleOrNull()?.status == "ACCEPTED"
    }

    fun getRegisteredDevices(userId: Long): SizedIterable<SmartAppUserDeviceEntity> {
        return transaction(database) {
            SmartAppUserDeviceEntity
                .find { SmartAppUserDevices.userId eq userId }
                .notForUpdate()
        }
    }
}