package me.jerry.urlrouter

import android.net.Uri
import java.util.concurrent.ConcurrentHashMap

/**
 * Maps URLs to Targets for routing.
 *
 * The mapping uses the scheme + host + path as the key.
 * Supports path parameters like "/user/{id}".
 */
class TargetMap {

    private val map = ConcurrentHashMap<String, Target>()

    /**
     * Add a mapping from source URL to target
     */
    fun add(sourceUrl: String, target: Target) {
        val key = normalizeKey(sourceUrl)
        map[key] = target
    }

    /**
     * Add multiple mappings at once
     */
    fun addAll(mappings: Map<String, Target>) {
        mappings.forEach { (url, target) ->
            add(url, target)
        }
    }

    /**
     * Find a target for the given URI
     */
    fun find(uri: Uri): Target? {
        // Try full match: scheme + host + path
        val fullKey = "${uri.scheme}://${uri.authority}${uri.path}"
        map[fullKey]?.let { return it }

        // Try path template match (e.g., "/user/{id}")
        val schemeHost = "${uri.scheme}://${uri.authority}"
        val uriPath = uri.path ?: ""
        for ((key, target) in map) {
            if (key.startsWith(schemeHost) && target.pathTemplate != null) {
                val templatePath = target.pathTemplate
                if (matchesPathTemplate(templatePath, uriPath)) {
                    return target
                }
            }
        }

        // Try host-only match
        uri.authority?.let { authority ->
            map["${uri.scheme}://$authority"]?.let { return it }
        }

        return null
    }

    /**
     * Check if a URI path matches a template path
     */
    private fun matchesPathTemplate(template: String, uriPath: String): Boolean {
        val templateSegments = template.split("/")
        val uriSegments = uriPath.split("/")

        if (templateSegments.size != uriSegments.size) return false

        for ((i, segment) in templateSegments.withIndex()) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                continue // Path parameter, matches anything
            }
            if (segment != uriSegments[i]) return false
        }

        return true
    }

    /**
     * Remove a mapping
     */
    fun remove(sourceUrl: String) {
        val key = normalizeKey(sourceUrl)
        map.remove(key)
    }

    /**
     * Clear all mappings
     */
    fun clear() {
        map.clear()
    }

    /**
     * Get all mappings
     */
    fun getAll(): Map<String, Target> {
        return map.toMap()
    }

    private fun normalizeKey(url: String): String {
        val uri = Uri.parse(url)
        return "${uri.scheme}://${uri.authority}${uri.path}"
    }
}
