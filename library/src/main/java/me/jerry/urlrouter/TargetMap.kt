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
        map[key] = enrichTarget(sourceUrl, target)
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
        val fullKey = "${uri.scheme}://${uri.authority}${uri.path}"
        map[fullKey]?.let { return it }

        val routeSegments = uri.routeSegments()
        for ((key, target) in map) {
            if (!key.startsWith("${uri.scheme}://")) continue

            val template = target.pathTemplate ?: continue
            if (matchesPathTemplate(template, routeSegments)) {
                return target
            }
        }

        uri.authority?.let { authority ->
            map["${uri.scheme}://$authority"]?.let { return it }
        }

        return null
    }

    /**
     * Check if a URI path matches a template path
     */
    private fun matchesPathTemplate(template: String, routeSegments: List<String>): Boolean {
        val templateSegments = template.routeSegments()

        if (templateSegments.size != routeSegments.size) return false

        for ((index, segment) in templateSegments.withIndex()) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                continue
            }
            if (segment != routeSegments[index]) return false
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

    private fun enrichTarget(sourceUrl: String, target: Target): Target {
        if (target.pathTemplate != null || !sourceUrl.contains('{')) {
            return target
        }

        val uri = Uri.parse(sourceUrl)
        val template = buildString {
            append('/')
            uri.authority?.takeIf { it.isNotEmpty() }?.let { append(it) }
            uri.path?.takeIf { it.isNotEmpty() }?.let { append(it) }
        }

        return target.copy(pathTemplate = template)
    }
}
