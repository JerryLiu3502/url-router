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
     * Create a stack navigation builder for pop/result flows.
     */
    fun stack(context: Context): StackNavigation {
        return StackNavigation(context, this)
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

        notifyNavigationStart(originalUri)

        if (isRequestIntercepted(originalUri)) {
            notifyRequestIntercepted(originalUri)
            return
        }

        val resolvedRoute = resolve(originalUri)

        if (resolvedRoute == null) {
            notifyTargetNotFound(originalUri)
            handleTargetNotFound(originalUri)
            return
        }

        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Resolved target: ${resolvedRoute.target.className}, uri: ${resolvedRoute.targetUri}")
        }

        notifyRouteResolved(originalUri, resolvedRoute)

        if (isTargetIntercepted(originalUri, resolvedRoute.targetUri)) {
            notifyTargetIntercepted(originalUri, resolvedRoute.targetUri)
            return
        }

        val intent = buildIntent(context, resolvedRoute, flags, extras)
        notifyIntentCreated(originalUri, intent)
        
        if (context is Activity) {
            context.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        notifyNavigationComplete(originalUri, intent)
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

        notifyNavigationStart(originalUri)

        if (isRequestIntercepted(originalUri)) {
            notifyRequestIntercepted(originalUri)
            return
        }

        val resolvedRoute = resolve(originalUri)

        if (resolvedRoute == null) {
            notifyTargetNotFound(originalUri)
            handleTargetNotFound(originalUri)
            return
        }

        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Resolved target: ${resolvedRoute.target.className}, uri: ${resolvedRoute.targetUri}")
        }

        notifyRouteResolved(originalUri, resolvedRoute)

        if (isTargetIntercepted(originalUri, resolvedRoute.targetUri)) {
            notifyTargetIntercepted(originalUri, resolvedRoute.targetUri)
            return
        }

        val intent = buildIntent(activity, resolvedRoute, flags, extras)
        notifyIntentCreated(originalUri, intent)
        
        activity.startActivityForResult(intent, requestCode)
        notifyNavigationComplete(originalUri, intent)
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

    private fun notifyNavigationStart(originalUri: Uri) {
        configuration.getRouteAspects().forEach { it.onNavigationStart(originalUri) }
    }

    private fun notifyRouteResolved(originalUri: Uri, resolvedRoute: ResolvedRoute) {
        configuration.getRouteAspects().forEach {
            it.onRouteResolved(originalUri, resolvedRoute.targetUri, resolvedRoute.target)
        }
    }

    private fun notifyIntentCreated(originalUri: Uri, intent: Intent) {
        configuration.getRouteAspects().forEach { it.onIntentCreated(originalUri, intent) }
    }

    private fun notifyNavigationComplete(originalUri: Uri, intent: Intent) {
        configuration.getRouteAspects().forEach { it.onNavigationComplete(originalUri, intent) }
    }

    private fun notifyTargetNotFound(originalUri: Uri) {
        configuration.getRouteAspects().forEach { it.onTargetNotFound(originalUri) }
    }

    private fun notifyRequestIntercepted(originalUri: Uri) {
        configuration.getRouteAspects().forEach { it.onRequestIntercepted(originalUri) }
    }

    private fun notifyTargetIntercepted(originalUri: Uri, resolvedUri: Uri) {
        configuration.getRouteAspects().forEach { it.onTargetIntercepted(originalUri, resolvedUri) }
    }
}

internal data class ResolvedRoute(
    val target: Target,
    val targetUri: Uri
)
