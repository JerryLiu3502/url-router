package me.jerry.urlrouter.sample

import android.app.Application
import android.widget.Toast
import me.jerry.urlrouter.Target
import me.jerry.urlrouter.TargetNotFoundHandler
import me.jerry.urlrouter.UrlRouter

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        UrlRouter.configuration()
            .setDebugEnabled(BuildConfig.DEBUG)
            .addTargetNotFoundHandler(object : TargetNotFoundHandler {
                override fun handle(uri: android.net.Uri): Boolean {
                    Toast.makeText(this@App, "Route not found: $uri", Toast.LENGTH_SHORT).show()
                    return true
                }
            })

        // Note: We don't need to manually map URLs here because
        // the AndroidManifest.xml already defines intent-filters
        // for the Activities. This sample demonstrates manual mapping.
        UrlRouter.apply(
            mapOf(
                "sample://home" to Target(MainActivity::class.java.name),
                "sample://target" to Target(TargetActivity::class.java.name),
                "sample://web" to Target(WebActivity::class.java.name)
            )
        )
    }
}
