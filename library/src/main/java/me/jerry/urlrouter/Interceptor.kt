package me.jerry.urlrouter

import android.net.Uri

/**
 * Interceptor for pre-processing URLs before routing.
 *
 * Use this to log, modify, or block URLs before they reach the target resolution.
 */
interface RequestInterceptor {
    /**
     * Process the incoming URI.
     *
     * @param uri The original URI
     * @return true if the URI has been handled (routing should stop), false to continue
     */
    fun intercept(uri: Uri): Boolean
}

/**
 * Interceptor for post-processing after target resolution.
 *
 * Use this to log, modify, or handle the resolved target before Intent is created.
 */
interface TargetInterceptor {
    /**
     * Process the resolved target URI.
     *
     * @param originalUri The original URI
     * @param targetUri The resolved target URI
     * @return true if the URI has been handled (routing should stop), false to continue
     */
    fun intercept(originalUri: Uri, targetUri: Uri): Boolean
}
