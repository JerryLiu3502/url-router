package me.jerry.urlrouter.sample

import android.app.Fragment
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MessageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val from = arguments?.getString("from") ?: "unknown"
        val message = arguments?.getString("message") ?: "none"
        val id = arguments?.getString("id") ?: "none"

        return TextView(activity).apply {
            text = "Fragment page\nFrom: $from\nMessage: $message\nId: $id"
            textSize = 20f
            gravity = Gravity.CENTER
        }
    }
}
