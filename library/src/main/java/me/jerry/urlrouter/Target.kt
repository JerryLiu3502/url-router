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
    val className: String = "",
    val schema: String = "",
    val pathTemplate: String? = null,
    val redirectTo: String? = null,
    val fragmentClassName: String? = null
) {
    companion object {
        /**
         * Create a Target from a Class
         */
        inline fun <reified T : android.app.Activity> create(schema: String = "", pathTemplate: String? = null): Target {
            return Target(T::class.java.name, schema, pathTemplate)
        }

        /**
         * Create a redirect target that resolves to another URI before intent creation.
         */
        fun redirect(destinationUrl: String, pathTemplate: String? = null): Target {
            return Target(redirectTo = destinationUrl, pathTemplate = pathTemplate)
        }

        /**
         * Create a fragment target hosted by an Activity.
         */
        fun fragment(
            hostActivityClassName: String,
            fragmentClassName: String,
            schema: String = "",
            pathTemplate: String? = null
        ): Target {
            return Target(
                className = hostActivityClassName,
                schema = schema,
                pathTemplate = pathTemplate,
                fragmentClassName = fragmentClassName
            )
        }
    }

    /**
     * Whether this mapping redirects to another URI.
     */
    fun isRedirect(): Boolean = !redirectTo.isNullOrBlank()

    /**
     * Whether this mapping points to an Activity target.
     */
    fun hasActivityTarget(): Boolean = className.isNotBlank()

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

        val routeSegments = originalUri.routeSegments()

        if (!originalUri.authority.isNullOrEmpty()) {
            builder.authority(originalUri.authority)
            originalUri.path?.let { builder.path(it) }
        } else if (routeSegments.isNotEmpty()) {
            builder.authority(routeSegments.first())
            routeSegments.drop(1)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "/", prefix = "/")
                ?.let { builder.path(it) }
        }

        // Copy query parameters
        originalUri.queryParameterNames.forEach { key ->
            originalUri.getQueryParameters(key).forEach { value ->
                builder.appendQueryParameter(key, value)
            }
        }

        originalUri.encodedFragment?.let { builder.encodedFragment(it) }

        return builder.build()
    }

    /**
     * Build the redirect URI for this target, preserving source params when possible.
     */
    fun buildRedirectUri(originalUri: Uri): Uri? {
        val destination = redirectTo?.takeIf { it.isNotBlank() } ?: return null
        val templateValues = collectTemplateValues(originalUri)

        var resolvedDestination = destination
        templateValues.forEach { (key, value) ->
            resolvedDestination = resolvedDestination.replace("{$key}", Uri.encode(value))
        }

        return mergeOriginalParams(Uri.parse(resolvedDestination), originalUri)
    }

    private fun collectTemplateValues(uri: Uri): Map<String, String> {
        val values = linkedMapOf<String, String>()
        val pathParams = extractPathParams(uri)

        pathParams.keySet().forEach { key ->
            pathParams.getString(key)?.let { values[key] = it }
        }

        uri.queryParameterNames.forEach { key ->
            uri.getQueryParameter(key)?.let { values.putIfAbsent(key, it) }
        }

        return values
    }

    private fun mergeOriginalParams(destinationUri: Uri, originalUri: Uri): Uri {
        if (originalUri.queryParameterNames.isEmpty() && originalUri.encodedFragment == null) {
            return destinationUri
        }

        val builder = destinationUri.buildUpon().clearQuery()
        val existingKeys = linkedSetOf<String>()

        destinationUri.queryParameterNames.forEach { key ->
            existingKeys += key
            destinationUri.getQueryParameters(key).forEach { value ->
                builder.appendQueryParameter(key, value)
            }
        }

        originalUri.queryParameterNames.forEach { key ->
            if (key in existingKeys) {
                return@forEach
            }

            originalUri.getQueryParameters(key).forEach { value ->
                builder.appendQueryParameter(key, value)
            }
        }

        if (destinationUri.encodedFragment == null) {
            originalUri.encodedFragment?.let { builder.encodedFragment(it) }
        }

        return builder.build()
    }
}

internal fun String.routeSegments(): List<String> {
    return trim('/')
        .split("/")
        .filter { it.isNotEmpty() }
}

internal fun String.isRoutePlaceholder(): Boolean {
    return startsWith("{") && endsWith("}")
}

internal fun Uri.routeSegments(): List<String> {
    return buildList {
        authority?.takeIf { it.isNotEmpty() }?.let { add(it) }
        pathSegments.filter { it.isNotEmpty() }.forEach { add(it) }
    }
}
