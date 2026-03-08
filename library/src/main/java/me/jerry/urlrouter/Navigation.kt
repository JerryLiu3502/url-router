package me.jerry.urlrouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * Receives a resolved Intent before navigation starts.
 */
fun interface IntentReceiver {
    fun onReceive(intent: Intent)
}

/**
 * Navigation builder for UrlRouter.
 */
class Navigation(
    private val context: Context,
    private val urlRouter: UrlRouter,
    uri: Uri
) {
    private var currentUri: Uri = uri
    private var flags: Int = 0
    private val extras = Bundle()

    /**
     * Set Intent flags
     */
    fun setFlags(flags: Int): Navigation {
        this.flags = flags
        return this
    }

    /**
     * Add a query parameter
     */
    fun appendQueryParameter(key: String, value: String): Navigation {
        currentUri = currentUri.buildUpon()
            .appendQueryParameter(key, value)
            .build()
        return this
    }

    /**
     * Add multiple query parameters.
     */
    fun appendQueryParameters(parameters: Map<String, String>): Navigation {
        var builder = currentUri.buildUpon()
        parameters.forEach { (key, value) ->
            builder = builder.appendQueryParameter(key, value)
        }
        currentUri = builder.build()
        return this
    }

    /**
     * Put an extra
     */
    fun putExtra(key: String, value: Any?): Navigation {
        when (value) {
            is String -> extras.putString(key, value)
            is Int -> extras.putInt(key, value)
            is Long -> extras.putLong(key, value)
            is Boolean -> extras.putBoolean(key, value)
            is Float -> extras.putFloat(key, value)
            is Double -> extras.putDouble(key, value)
            is Bundle -> extras.putBundle(key, value)
        }
        return this
    }

    /**
     * Put all extras from a Bundle
     */
    fun putExtras(bundle: Bundle): Navigation {
        extras.putAll(bundle)
        return this
    }

    /**
     * Put all extras from an existing Intent.
     */
    fun putExtras(intent: Intent): Navigation {
        intent.extras?.let(extras::putAll)
        return this
    }

    /**
     * Start the navigation
     */
    fun start() {
        urlRouter.startNavigation(context, currentUri, flags, extras)
    }

    /**
     * Check whether the current route resolves to an Activity target.
     */
    fun hasTarget(): Boolean {
        return urlRouter.hasTarget(currentUri)
    }

    /**
     * Build the Intent that would be used for navigation without starting it.
     */
    fun getIntent(): Intent? {
        return urlRouter.createIntent(context, currentUri, flags, extras)
    }

    /**
     * Invoke the receiver when a resolved intent exists.
     */
    fun ifIntentNonNullSendTo(receiver: IntentReceiver): Navigation {
        getIntent()?.let(receiver::onReceive)
        return this
    }

    /**
     * Start navigation for result
     *
     * @param activity The activity to start for result
     * @param requestCode Request code for result identification
     */
    fun startForResult(activity: Activity, requestCode: Int) {
        urlRouter.startNavigationForResult(activity, currentUri, flags, extras, requestCode)
    }
}
