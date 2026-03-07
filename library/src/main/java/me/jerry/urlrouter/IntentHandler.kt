package me.jerry.urlrouter

import android.content.Context
import android.content.Intent

/**
 * Handles the creation of Intent from a target URI.
 */
interface IntentHandler {
    /**
     * Create an Intent from the target URI.
     *
     * @param context The context
     * @param target The resolved target
     * @param uri The target URI with parameters
     * @return The created Intent
     */
    fun createIntent(context: Context, target: Target, uri: android.net.Uri): Intent
}
