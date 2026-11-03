package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "AppConfigRepository"
private const val COLLECTION_CONFIG = "app_config"
private const val DOC_SETTINGS = "settings"
private const val KEY_TARGET_URL = "targetUrl"
private const val KEY_UPDATED_AT = "updatedAt"
private const val KEY_UPDATED_BY = "updatedBy"

private const val PREFS_ADMIN = "admin_kiosk_prefs"
private const val PREF_CACHED_URL = "cached_synced_url"
private const val PREF_ADMIN_PASSWORD = "admin_password"
private const val DEFAULT_ADMIN_PASSWORD = "admin"

class AppConfigRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_ADMIN, Context.MODE_PRIVATE)
    }

    /**
     * Gets the last known cached URL or default fallback if offline
     */
    fun getCachedTargetUrl(defaultFallback: String): String {
        return prefs.getString(PREF_CACHED_URL, null) ?: defaultFallback
    }

    /**
     * Sets or updates cached URL locally
     */
    fun saveCachedTargetUrl(url: String) {
        prefs.edit().putString(PREF_CACHED_URL, url).apply()
    }

    /**
     * Verifies admin password (default: 'admin')
     */
    fun verifyAdminPassword(password: String): Boolean {
        val storedPassword = prefs.getString(PREF_ADMIN_PASSWORD, DEFAULT_ADMIN_PASSWORD) ?: DEFAULT_ADMIN_PASSWORD
        return password == storedPassword
    }

    /**
     * Updates admin password
     */
    fun updateAdminPassword(newPassword: String) {
        prefs.edit().putString(PREF_ADMIN_PASSWORD, newPassword).apply()
    }

    /**
     * Observes real-time updates to target URL from Firestore.
     * All devices running this flow will receive live changes.
     */
    fun observeTargetUrl(defaultFallback: String): Flow<String> = callbackFlow {
        val docRef = db.collection(COLLECTION_CONFIG).document(DOC_SETTINGS)
        val initialCache = getCachedTargetUrl(defaultFallback)
        trySend(initialCache)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Firestore snapshot listener error: ${error.message}", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val url = snapshot.getString(KEY_TARGET_URL)
                if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    saveCachedTargetUrl(url)
                    trySend(url)
                }
            } else if (snapshot != null && !snapshot.exists()) {
                // Document not yet initialized on backend, initialize with default
                Log.d(TAG, "Config settings document does not exist yet.")
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Updates target URL in Firestore so that all synced devices switch to it.
     */
    suspend fun updateTargetUrl(newUrl: String, adminName: String = "Admin"): Result<Unit> {
        return try {
            val trimmedUrl = newUrl.trim()
            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                return Result.failure(IllegalArgumentException("URL harus diawali dengan http:// atau https://"))
            }

            val docRef = db.collection(COLLECTION_CONFIG).document(DOC_SETTINGS)
            val data = mapOf(
                KEY_TARGET_URL to trimmedUrl,
                KEY_UPDATED_AT to FieldValue.serverTimestamp(),
                KEY_UPDATED_BY to adminName
            )

            docRef.set(data, SetOptions.merge()).await()
            saveCachedTargetUrl(trimmedUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update target URL in Firestore", e)
            Result.failure(e)
        }
    }
}
