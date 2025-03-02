package dev.kuro9.domain.smartapp.user.service

import dev.kuro9.domain.smartapp.user.repository.SmartAppUserCredentialEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SmartAppUserService {

    /**
     * @return user smartapp token or null
     */
    fun getUserCredential(userId: Long): String? {
        return SmartAppUserCredentialEntity.findById(userId)?.smartAppToken
    }
}