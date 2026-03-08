package me.jerry.urlrouter

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * IntentHandler that preserves fragment target metadata for a host Activity.
 */
class FragmentIntentHandler(
    private val delegate: IntentHandler = DefaultIntentHandler()
) : IntentHandler {

    override fun createIntent(context: Context, target: Target, uri: Uri): Intent {
        return delegate.createIntent(context, target, uri).apply {
            target.fragmentClassName?.let { putExtra(EXTRA_FRAGMENT_CLASS_NAME, it) }
        }
    }

    companion object {
        const val EXTRA_FRAGMENT_CLASS_NAME = "urlrouter.fragment.className"
    }
}
