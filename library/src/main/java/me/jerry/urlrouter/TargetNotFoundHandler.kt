package me.jerry.urlrouter

import android.net.Uri

/**
 * Handler for cases when no target is found for a URL.
 *
 * Multiple handlers can be registered - they will be called in order until one handles the URL.
 */
interface TargetNotFoundHandler {
    /**
     * Handle a URL that has no registered target.
     *
     * @param uri The unresolved URI
     * @return true if the URL has been handled, false to continue to next handler
     */
    fun handle(uri: Uri): Boolean
}
