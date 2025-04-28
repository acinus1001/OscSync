package dev.kuro9.domain.ai.log.service

interface GoogleAiKeychainStorage {

    fun getRootKey(refKey: String): String?
    fun addKeychain(rootKey: String, refKey: String, key: String)
}