package me.jerry.urlrouter.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class TargetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)

        val from = intent.getStringExtra("from") ?: "unknown"
        findViewById<TextView>(R.id.tv_from).text = "From: $from"
    }
}
