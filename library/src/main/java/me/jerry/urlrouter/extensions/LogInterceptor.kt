package me.jerry.urlrouter.extensions

import android.net.Uri
import android.util.Log
import me.jerry.urlrouter.RequestInterceptor
import me.jerry.urlrouter.TargetInterceptor

/**
 * Simple logging interceptor for debugging.
 */
class LogInterceptor(private val tag: String = "UrlRouter") : RequestInterceptor, TargetInterceptor {

    override fun intercept(uri: Uri): Boolean {
        Log.d(tag, "[Request] $uri")
        return false // Continue processing
    }

    override fun intercept(originalUri: Uri, targetUri: Uri): Boolean {
        Log.d(tag, "[Target] $originalUri -> $targetUri")
        return false // Continue processing
    }
}
