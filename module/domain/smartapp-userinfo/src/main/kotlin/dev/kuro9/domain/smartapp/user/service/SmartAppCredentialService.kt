package dev.kuro9.domain.smartapp.user.service

import dev.kuro9.domain.smartapp.user.exception.SmartAppUserException.CredentialNotFoundException
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserCredentialEntity
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SmartAppCredentialService {

    /**
     * @return user smartapp token or null
     */
    fun getUserCredentialOrNull(userId: Long): SmartAppToken? {
        return SmartAppUserCredentialEntity.findById(userId)?.smartAppToken?.let(SmartAppToken::of)
    }

    /**
     * @return user smartapp token
     */
    @Throws(CredentialNotFoundException::class)
    fun getUserCredential(userId: Long): SmartAppToken =
        getUserCredentialOrNull(userId) ?: throw CredentialNotFoundException("User token not found")

}