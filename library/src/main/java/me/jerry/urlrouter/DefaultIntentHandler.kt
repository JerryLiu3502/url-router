package me.jerry.urlrouter

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Default implementation of IntentHandler.
 */
class DefaultIntentHandler : IntentHandler {

    override fun createIntent(context: Context, target: Target, uri: Uri): Intent {
        require(target.hasActivityTarget()) { "Activity target must define className" }

        val pathParams = target.extractPathParams(uri)

        return Intent(Intent.ACTION_VIEW).apply {
            data = uri
            setClassName(context, target.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Add path parameters from template
            pathParams.keySet().forEach { key ->
                putExtra(key, pathParams.getString(key))
            }

            // Add query parameters
            uri.queryParameterNames.forEach { key ->
                val values = uri.getQueryParameters(key)
                when (values.size) {
                    0 -> Unit
                    1 -> putExtra(key, values.first())
                    else -> putStringArrayListExtra(key, ArrayList(values))
                }
            }
        }
    }
}
