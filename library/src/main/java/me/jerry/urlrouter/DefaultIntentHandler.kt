package me.jerry.urlrouter

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Default implementation of IntentHandler.
 */
class DefaultIntentHandler : IntentHandler {

    override fun createIntent(context: Context, target: Target, uri: Uri): Intent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            setClassName(context, target.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }
}
