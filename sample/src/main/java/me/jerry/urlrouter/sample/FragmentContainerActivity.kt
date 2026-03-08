package me.jerry.urlrouter.sample

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import me.jerry.urlrouter.FragmentIntentHandler

class FragmentContainerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)

        if (savedInstanceState != null) return

        val fragmentClassName = intent.getStringExtra(FragmentIntentHandler.EXTRA_FRAGMENT_CLASS_NAME)
            ?: return

        val args = Bundle(intent.extras ?: Bundle()).apply {
            remove(FragmentIntentHandler.EXTRA_FRAGMENT_CLASS_NAME)
        }

        val fragment = android.app.Fragment.instantiate(this, fragmentClassName, args)

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
