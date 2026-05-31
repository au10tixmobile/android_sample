package com.au10tix.integration.sample.sdk

import android.content.Context
import android.util.Log
import com.au10tix.localinfer.utils.LocalSdcManager
import com.au10tix.sdk.commons.Au10Error
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.OnPrepareCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume

private const val TAG = "Au10tixSdkManager"

object Au10tixSdkManager {

    private val isInitializedState = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = isInitializedState.asStateFlow()

    private val isLoadingState = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = isLoadingState.asStateFlow()

    private val sessionIdState = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = sessionIdState.asStateFlow()

    private val organizationIdState = MutableStateFlow<String?>(null)
    val organizationId: StateFlow<String?> = organizationIdState.asStateFlow()

    private val errorMessageState = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = errorMessageState.asStateFlow()

    /**
     * Initializes the SDK with the full workflow JSON response from the backend.
     */
    suspend fun initialize(context: Context, workflowResponse: String) {
        if (isLoadingState.value) return
        isLoadingState.value = true
        errorMessageState.value = null

        val json = try {
            JSONObject(workflowResponse.trim())
        } catch (e: JSONException) {
            errorMessageState.value = "Invalid JSON — paste the full workflow response"
            isLoadingState.value = false
            return
        }

        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { isLoadingState.value = false }
            try {
                Au10xCore.prepare(context, json, object : OnPrepareCallback {
                    override fun onPrepared(sessionId: String) {
                        sessionIdState.value = sessionId
                        val claims = Au10xCore.getSessionClaims()
                        organizationIdState.value = claims?.clientOrganizationId
                        isInitializedState.value = true
                        isLoadingState.value = false
                        LocalSdcManager.initialize(context.applicationContext) { ok, error ->
                            if (!ok) {
                                Log.e(TAG, "LocalSdcManager init failed: ${error?.errorMessage ?: "unknown"}")
                            }
                        }
                        Log.d(TAG, "SDK initialized — sessionId: $sessionId")
                        continuation.resume(Unit)
                    }

                    override fun onPrepareError(error: Au10Error) {
                        val message = error.toString().ifBlank { "Initialization failed" }
                        errorMessageState.value = message
                        isLoadingState.value = false
                        Log.e(TAG, "SDK initialization failed: $message")
                        continuation.resume(Unit)
                    }
                })
            } catch (e: Exception) {
                errorMessageState.value = e.message ?: "Initialization failed"
                isLoadingState.value = false
                continuation.resume(Unit)
            }
        }
    }

    fun clearError() {
        errorMessageState.value = null
    }

    fun reset() {
        isInitializedState.value = false
        sessionIdState.value = null
        organizationIdState.value = null
        errorMessageState.value = null
        isLoadingState.value = false
    }

}
