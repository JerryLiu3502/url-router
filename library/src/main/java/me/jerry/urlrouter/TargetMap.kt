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

    private val routes = ConcurrentHashMap<String, RegisteredRoute>()

    /**
     * Add a mapping from source URL to target
     */
    fun add(sourceUrl: String, target: Target) {
        val key = normalizeKey(sourceUrl)
        val enrichedTarget = enrichTarget(sourceUrl, target)
        routes[key] = RegisteredRoute(
            sourceUri = Uri.parse(sourceUrl),
            target = enrichedTarget
        )
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
        val candidates = routes.values.mapNotNull { route ->
            route.scoreAgainst(uri)?.let { score -> RouteCandidate(route, score) }
        }

        val bestCandidate = candidates.maxByOrNull { it.score } ?: return null
        val hasTie = candidates.count { it.score == bestCandidate.score } > 1

        return if (hasTie) null else bestCandidate.route.target
    }

    /**
     * Remove a mapping
     */
    fun remove(sourceUrl: String) {
        val key = normalizeKey(sourceUrl)
        routes.remove(key)
    }

    /**
     * Clear all mappings
     */
    fun clear() {
        routes.clear()
    }

    /**
     * Get all mappings
     */
    fun getAll(): Map<String, Target> {
        return routes.mapValues { it.value.target }
    }

    private fun normalizeKey(url: String): String {
        val uri = Uri.parse(url)
        return buildString {
            uri.scheme?.takeIf { it.isNotEmpty() }?.let {
                append(it)
                append("://")
            }
            append(uri.routeSegments().joinToString("/"))
        }
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

    private data class RegisteredRoute(
        val sourceUri: Uri,
        val target: Target
    ) {
        private val sourceSegments = sourceUri.routeSegments()
        private val templateSegments = target.pathTemplate?.routeSegments()
        private val literalSegmentCount = templateSegments?.count { !it.isRoutePlaceholder() } ?: sourceSegments.size

        fun scoreAgainst(incomingUri: Uri): RouteScore? {
            val incomingSegments = incomingUri.routeSegments()
            val pathMatches = if (templateSegments != null) {
                templateMatchesRoute(target.pathTemplate.orEmpty(), incomingSegments)
            } else {
                sourceSegments == incomingSegments
            }

            if (!pathMatches) {
                return null
            }

            val schemeScore = when {
                !incomingUri.scheme.isNullOrEmpty() && sourceUri.scheme == incomingUri.scheme -> 4
                sourceUri.scheme.isNullOrEmpty() -> 3
                incomingUri.scheme.isNullOrEmpty() -> 2
                else -> 1
            }

            val templateScore = if (templateSegments == null) 2 else 1
            val authorityScore = if (!sourceUri.authority.isNullOrEmpty()) 1 else 0

            return RouteScore(
                schemeScore = schemeScore,
                templateScore = templateScore,
                literalSegmentCount = literalSegmentCount,
                authorityScore = authorityScore
            )
        }
    }

    private data class RouteCandidate(
        val route: RegisteredRoute,
        val score: RouteScore
    )

    private data class RouteScore(
        val schemeScore: Int,
        val templateScore: Int,
        val literalSegmentCount: Int,
        val authorityScore: Int
    ) : Comparable<RouteScore> {
        override fun compareTo(other: RouteScore): Int {
            return compareValuesBy(
                this,
                other,
                RouteScore::schemeScore,
                RouteScore::templateScore,
                RouteScore::literalSegmentCount,
                RouteScore::authorityScore
            )
        }
    }
}

private fun templateMatchesRoute(template: String, routeSegments: List<String>): Boolean {
    val templateSegments = template.routeSegments()

    if (templateSegments.size != routeSegments.size) return false

    for ((index, segment) in templateSegments.withIndex()) {
        if (segment.isRoutePlaceholder()) {
            continue
        }
        if (segment != routeSegments[index]) return false
    }

    return true
}
