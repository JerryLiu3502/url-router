package me.jerry.urlrouter

import android.content.ComponentName
import android.net.Uri

/**
 * Represents a target Activity mapping.
 *
 * @param className The fully qualified class name of the target Activity
 * @param schema The URL scheme (e.g., "myapp", "https")
 */
data class Target(
    val className: String,
    val schema: String = ""
) {
    companion object {
        /**
         * Create a Target from a Class
         */
        inline fun <reified T : android.app.Activity> create(schema: String = ""): Target {
            return Target(T::class.java.name, schema)
        }
    }

    /**
     * Build the final URI for this target, merging original params
     */
    fun buildUri(originalUri: Uri): Uri {
        val builder = if (schema.isNotEmpty()) {
            Uri.parse("$schema://").buildUpon()
        } else {
            Uri.parse("app://").buildUpon()
        }
        
        // Use original host and path
        originalUri.host?.let { builder.authority(it) }
        originalUri.path?.let { builder.path(it) }
        
        // Copy query parameters
        originalUri.queryParameterNames.forEach { key ->
            originalUri.getQueryParameters(key).forEach { value ->
                builder.appendQueryParameter(key, value)
            }
        }
        
        return builder.build()
    }

    /**
     * Get the ComponentName for starting the Activity
     */
    fun toComponentName(): ComponentName {
        return ComponentName(android.app.Application::class.java.`package`.name, className)
    }
}
