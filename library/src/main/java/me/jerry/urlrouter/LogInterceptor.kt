package me.jerry.urlrouter

import android.net.Uri
import android.util.Log

/**
 * A simple RequestInterceptor that logs incoming URIs.
 */
class LogInterceptor(
    private val tag: String = "UrlRouter"
) : RequestInterceptor {

    override fun intercept(uri: Uri): Boolean {
        Log.d(tag, "Routing: $uri")
        return false // Continue routing
    }
}
