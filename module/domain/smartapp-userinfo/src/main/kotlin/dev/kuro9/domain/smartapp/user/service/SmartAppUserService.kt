package dev.kuro9.domain.smartapp.user.service

import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.*
import dev.kuro9.domain.smartapp.user.exception.SmartAppUserException.CredentialNotFoundException
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserDeviceEntity
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserDevices
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponseObject.DeviceInfo
import dev.kuro9.internal.smartapp.api.exception.ApiNotSuccessException
import dev.kuro9.internal.smartapp.api.service.SmartAppApiService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
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

    fun getUserRegisteredDevices(userId: Long): List<SmartAppUserDeviceEntity> {
        return transaction(database) {
            SmartAppUserDeviceEntity
                .find { SmartAppUserDevices.userId eq userId }
                .notForUpdate()
                .toList()
        }
    }

    @Throws(CredentialNotFoundException::class)
    suspend fun getUserDevice(userId: Long, deviceId: String): DeviceInfo {
        return apiClient.getDeviceInfo(
            smartAppToken = userCredentialService.getUserCredential(userId),
            deviceId = deviceId,
        )
    }

    @Transactional
    @CacheEvict(cacheNames = ["smartapp-registered-devices"])
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
    @CacheEvict(cacheNames = ["smartapp-registered-devices"])
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

    @Throws(NotSupportException::class)
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

    @Throws(NotFoundException::class, NotSupportException::class)
    suspend fun executeDeviceByName(
        userId: Long,
        deviceName: String,
        desireState: Boolean,
    ) {
        val device = transaction(database) {
            getRegisteredDeviceByName(userId, deviceName)
                ?: throw NotFoundException("device name $deviceName not found.")
        }

        executeDevice(
            userId = userId,
            deviceId = device.deviceId,
            deviceComponentId = "main",
            deviceCapabilityId = "switch",
            desireState = desireState,
        )
    }

    /**
     * deviceName으로 사용자 기기 삭제
     *
     * @return 삭제 row 존재 여부
     */
    @Transactional
    @CacheEvict(cacheNames = ["smartapp-registered-devices"])
    suspend fun deleteDeviceByName(userId: Long, deviceName: String): Boolean {
        return transaction(database) {
            Op.build { SmartAppUserDevices.userId eq userId }
                .and { SmartAppUserDevices.deviceName eq deviceName }
                .let { op -> SmartAppUserDevices.deleteWhere { op } } != 0
        }
    }

    fun getRegisteredDeviceByName(
        userId: Long,
        deviceName: String,
    ): SmartAppUserDeviceEntity? {
        return transaction(database) {
            Op.build { SmartAppUserDevices.userId eq userId }
                .and { SmartAppUserDevices.deviceName eq deviceName }
                .and { SmartAppUserDevices.deviceComponent eq "main" }
                .and { SmartAppUserDevices.deviceCapability eq "switch" }
                .let(SmartAppUserDeviceEntity::find)
                .singleOrNull()
        }
    }

    @Cacheable("smartapp-registered-devices")
    fun getRegisteredDevices(userId: Long): List<SmartAppUserDeviceEntity> {
        return transaction(database) {
            SmartAppUserDeviceEntity
                .find { SmartAppUserDevices.userId eq userId }
                .notForUpdate()
                .toList()
        }
    }

    fun saveUserCredential(userId: Long, smartAppToken: String) {
        userCredentialService.saveUserCredential(userId, smartAppToken)
    }

    fun deleteUserCredential(userId: Long) {
        userCredentialService.deleteUserCredential(userId)
    }
}