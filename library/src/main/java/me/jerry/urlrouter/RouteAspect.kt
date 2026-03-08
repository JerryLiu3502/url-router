package me.jerry.urlrouter

import android.content.Intent
import android.net.Uri

/**
 * Lightweight AOP hooks for observing the routing lifecycle.
 *
 * All callbacks are optional and should stay side-effect friendly.
 */
interface RouteAspect {
    /** Called before interceptors/target resolution begin. */
    fun onNavigationStart(originalUri: Uri) = Unit

    /** Called after a target has been resolved, before target interceptors. */
    fun onRouteResolved(originalUri: Uri, resolvedUri: Uri, target: Target) = Unit

    /** Called after the final Intent has been created. */
    fun onIntentCreated(originalUri: Uri, intent: Intent) = Unit

    /** Called after startActivity/startActivityForResult has been invoked. */
    fun onNavigationComplete(originalUri: Uri, intent: Intent) = Unit

    /** Called when no route target can be resolved. */
    fun onTargetNotFound(originalUri: Uri) = Unit

    /** Called when request interception stops navigation. */
    fun onRequestIntercepted(originalUri: Uri) = Unit

    /** Called when target interception stops navigation. */
    fun onTargetIntercepted(originalUri: Uri, resolvedUri: Uri) = Unit
}
