package me.jerry.urlrouter

import android.net.Uri
import android.os.Bundle

/**
 * Represents a target Activity mapping.
 *
 * @param className The fully qualified class name of the target Activity
 * @param schema The URL scheme (e.g., "myapp", "https")
 * @param pathTemplate Optional path template with parameters like "/user/{id}"
 */
data class Target(
    val className: String,
    val schema: String = "",
    val pathTemplate: String? = null
) {
    companion object {
        /**
         * Create a Target from a Class
         */
        inline fun <reified T : android.app.Activity> create(schema: String = "", pathTemplate: String? = null): Target {
            return Target(T::class.java.name, schema, pathTemplate)
        }
    }

    /**
     * Extract path parameters from the URI based on the path template
     */
    fun extractPathParams(uri: Uri): Bundle {
        val bundle = Bundle()
        val template = pathTemplate ?: return bundle

        val templateSegments = template.routeSegments()
        val uriSegments = uri.routeSegments()

        if (templateSegments.size != uriSegments.size) return bundle

        for ((index, segment) in templateSegments.withIndex()) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                val paramName = segment.drop(1).dropLast(1)
                bundle.putString(paramName, uriSegments[index])
            }
        }

        return bundle
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
}

internal fun String.routeSegments(): List<String> {
    return trim('/')
        .split("/")
        .filter { it.isNotEmpty() }
}

internal fun Uri.routeSegments(): List<String> {
    return buildList {
        authority?.takeIf { it.isNotEmpty() }?.let { add(it) }
        pathSegments.filter { it.isNotEmpty() }.forEach { add(it) }
    }
}
