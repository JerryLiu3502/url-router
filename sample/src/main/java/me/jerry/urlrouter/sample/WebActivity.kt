package me.jerry.urlrouter.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class WebActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        val url = intent.data?.getQueryParameter("url") ?: "none"
        findViewById<TextView>(R.id.tv_url).text = "URL: $url"
    }
}
