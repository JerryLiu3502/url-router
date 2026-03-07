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

        // Step 1: Request interceptors
        for (interceptor in configuration.getRequestInterceptors()) {
            if (interceptor.intercept(originalUri)) {
                if (configuration.debugEnabled) {
                    Log.d("UrlRouter", "Request intercepted, stopping navigation")
                }
                return
            }
        }

        // Step 2: Find target
        val target = targetMap.find(originalUri)

        if (target == null) {
            // Step 3: Target not found - try handlers
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
            return
        }

        // Step 4: Build target URI
        val targetUri = target.buildUri(originalUri)

        if (configuration.debugEnabled) {
            Log.d("UrlRouter", "Resolved target: ${target.className}, uri: $targetUri")
        }

        // Step 5: Target interceptors
        for (interceptor in configuration.getTargetInterceptors()) {
            if (interceptor.intercept(originalUri, targetUri)) {
                if (configuration.debugEnabled) {
                    Log.d("UrlRouter", "Target intercepted, stopping navigation")
                }
                return
            }
        }

        // Step 6: Create and start Intent
        val intent = configuration.intentHandler.createIntent(context, target, targetUri)
        intent.putExtras(extras)
        if (flags != 0) {
            intent.addFlags(flags)
        }
        
        if (context is Activity) {
            context.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
