package me.jerry.urlrouter.sample

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import me.jerry.urlrouter.UrlRouter

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_navigate_target).setOnClickListener {
            UrlRouter.navigation(this, "sample://target")
                .putExtra("from", "MainActivity")
                .start()
        }

        findViewById<Button>(R.id.btn_navigate_web).setOnClickListener {
            UrlRouter.navigation(this, "sample://web?url=https://example.com")
                .start()
        }

        findViewById<Button>(R.id.btn_navigate_missing).setOnClickListener {
            UrlRouter.navigation(this, "sample://missing")
                .start()
        }
    }
}
