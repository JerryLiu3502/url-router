package me.jerry.urlrouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log

/**
 * Main entry point for UrlRouter.
 *
 * Usage:
 * ```
 * UrlRouter.configuration()
 *     .setDebugEnabled(true)
 *     .addRequestInterceptor(LogInterceptor())
 *
 * UrlRouter.apply(
 *     mapOf("myapp://home" to Target(HomeActivity::class.java.name))
 * )
 *
 * UrlRouter.navigation(context, "myapp://home").start()
 * ```
 */
object UrlRouter {

    private const val maxRedirectHops = 8
    private val configuration = Configuration()
    private val targetMap = TargetMap()

    /**
     * Get the configuration object
     */
    fun configuration(): Configuration = configuration

    /**
     * Apply URL mappings
     *
     * @param mappings Map of source URL to Target
     */
    fun apply(mappings: Map<String, Target>) {
        targetMap.addAll(mappings)
    }

    /**
     * Apply a single mapping
     */
    fun apply(url: String, target: Target) {
        targetMap.add(url, target)
    }

    /**
     * Clear all mappings
     */
    fun clear() {
        targetMap.clear()
    }

    /**
     * Remove a mapping by URL
     */
    fun remove(url: String) {
        targetMap.remove(url)
    }

    /**
     * Check if a URL can be opened (has a matching target)
     */
    fun canOpen(url: String): Boolean {
        val uri = Uri.parse(url)
        return resolve(uri) != null
    }

    /**
     * Check if a URI can be opened (has a matching target)
     */
    fun canOpen(uri: Uri): Boolean {
        return resolve(uri) != null
    }

    /**
     * Create a Navigation for the given URL
     */
    fun navigation(context: Context, url: String): Navigation {
        val uri = Uri.parse(url)
        return Navigation(context, this, uri)
    }

    /**
     * Create a Navigation for the given URI
     */
    fun navigation(context: Context, uri: Uri): Navigation {
        return Navigation(context, this, uri)
    }

    /**
     * Pop back the current activity and optionally navigate to a fallback URL
     * 
     * @param context Context
     * @param fallbackUrl Optional URL to navigate to after popping back
     * @return true if popped back successfully, false otherwise
     */
    fun popBack(context: Context, fallbackUrl: String? = null): Boolean {
        if (context is Activity) {
            if (fallbackUrl != null) {
                // Pop back then navigate to fallback
                context.finish()
                navigation(context, fallbackUrl).start()
                return true
            } else {
                // Just pop back
                context.finish()
                return true
            }
        }
        return false
    }

    /**
     * Internal method to start navigation
     */
    internal fun startNavigation(
        context: Context,
        originalUri: Uri,
        flags: Int,
        extras: Bundle
    ) {
        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Navigating to: $originalUri")
        }

        if (isRequestIntercepted(originalUri)) {
            return
        }

        val resolvedRoute = resolve(originalUri)

        if (resolvedRoute == null) {
            handleTargetNotFound(originalUri)
            return
        }

        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Resolved target: ${resolvedRoute.target.className}, uri: ${resolvedRoute.targetUri}")
        }

        if (isTargetIntercepted(originalUri, resolvedRoute.targetUri)) {
            return
        }

        val intent = buildIntent(context, resolvedRoute, flags, extras)
        
        if (context is Activity) {
            context.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Internal method to start navigation for result
     */
    internal fun startNavigationForResult(
        activity: Activity,
        originalUri: Uri,
        flags: Int,
        extras: Bundle,
        requestCode: Int
    ) {
        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Navigating for result to: $originalUri, requestCode=$requestCode")
        }

        if (isRequestIntercepted(originalUri)) {
            return
        }

        val resolvedRoute = resolve(originalUri)

        if (resolvedRoute == null) {
            handleTargetNotFound(originalUri)
            return
        }

        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Resolved target: ${resolvedRoute.target.className}, uri: ${resolvedRoute.targetUri}")
        }

        if (isTargetIntercepted(originalUri, resolvedRoute.targetUri)) {
            return
        }

        val intent = buildIntent(activity, resolvedRoute, flags, extras)
        
        activity.startActivityForResult(intent, requestCode)
    }

    internal fun hasTarget(uri: Uri): Boolean {
        return resolve(uri) != null
    }

    internal fun createIntent(
        context: Context,
        uri: Uri,
        flags: Int,
        extras: Bundle
    ): Intent? {
        val resolvedRoute = resolve(uri) ?: return null
        return buildIntent(context, resolvedRoute, flags, extras)
    }

    internal fun resolve(uri: Uri): ResolvedRoute? {
        var currentUri = uri
        val visited = linkedSetOf(uri.toString())

        repeat(maxRedirectHops) {
            val target = targetMap.find(currentUri) ?: return null
            val redirectUri = target.buildRedirectUri(currentUri)

            if (redirectUri == null) {
                if (!target.hasActivityTarget()) {
                    return null
                }

                return ResolvedRoute(
                    target = target,
                    targetUri = target.buildUri(currentUri)
                )
            }

            if (!visited.add(redirectUri.toString())) {
                if (configuration.debugEnabled) {
                    Log.w("UrlRouter", "Redirect loop detected for: $uri")
                }
                return null
            }

            currentUri = redirectUri
        }

        if (configuration.debugEnabled) {
            Log.w("UrlRouter", "Too many redirect hops for: $uri")
        }

        return null
    }

    private fun isRequestIntercepted(originalUri: Uri): Boolean {
        for (interceptor in configuration.getRequestInterceptors()) {
            if (interceptor.intercept(originalUri)) {
                if (configuration.debugEnabled) {
                    Log.d("UrlRouter", "Request intercepted, stopping navigation")
                }
                return true
            }
        }

        return false
    }

    private fun isTargetIntercepted(originalUri: Uri, targetUri: Uri): Boolean {
        for (interceptor in configuration.getTargetInterceptors()) {
            if (interceptor.intercept(originalUri, targetUri)) {
                if (configuration.debugEnabled) {
                    Log.d("UrlRouter", "Target intercepted, stopping navigation")
                }
                return true
            }
        }

        return false
    }

    private fun handleTargetNotFound(originalUri: Uri) {
        var handled = false
        for (handler in configuration.getTargetNotFoundHandlers()) {
            if (handler.handle(originalUri)) {
                handled = true
                break
            }
        }

        if (!handled && configuration.debugEnabled) {
            Log.w("UrlRouter", "No target found for: $originalUri")
        }
    }

    private fun buildIntent(
        context: Context,
        resolvedRoute: ResolvedRoute,
        flags: Int,
        extras: Bundle
    ): Intent {
        return configuration.intentHandler.createIntent(context, resolvedRoute.target, resolvedRoute.targetUri).apply {
            putExtras(extras)
            if (flags != 0) {
                addFlags(flags)
            }
        }
    }
}

internal data class ResolvedRoute(
    val target: Target,
    val targetUri: Uri
)
