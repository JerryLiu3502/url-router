package me.jerry.urlrouter

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle

/**
 * Navigation builder for UrlRouter.
 */
class Navigation(
    private val context: Context,
    private val urlRouter: UrlRouter,
    private val uri: Uri
) {
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
        val builder = uri.buildUpon()
        builder.appendQueryParameter(key, value)
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
     * Start the navigation
     */
    fun start() {
        urlRouter.startNavigation(context, uri, flags, extras)
    }
}
