package me.jerry.urlrouter.sample

import android.app.Application
import me.jerry.urlrouter.Target
import me.jerry.urlrouter.UrlRouter

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        UrlRouter.configuration()
            .setDebugEnabled(BuildConfig.DEBUG)

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
