package com.privateinternetaccess.account.internals.persistency.datastore

/**
 * Platform-backed, encrypted string key/value store used by [TokenPersistence].
 *
 * On Android this is backed by a Preferences DataStore whose values are encrypted with a
 * Tink AEAD primitive wrapping an Android Keystore key.
 */
internal expect object EncryptedKeyValueStore {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)

    /**
     * Last known value for [key], served from an in-memory cache without suspending.
     * Populated on store initialization and kept up to date on every write/remove.
     */
    fun cachedString(key: String): String?
}