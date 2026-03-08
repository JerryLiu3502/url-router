package me.jerry.urlrouter

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

/**
 * Builder for stack operations inspired by Floo.stack(...).
 *
 * Supports:
 * - finishing the top N tracked activities
 * - optionally delivering a result to the current activity before finishing
 * - optionally navigating to another route after the stack operation completes
 */
class StackNavigation internal constructor(
    private val context: Context,
    private val urlRouter: UrlRouter
) {
    private var popCount: Int = 1
    private var targetUrl: String? = null
    private var resultCode: Int = Activity.RESULT_OK
    private val resultExtras = Bundle()

    /**
     * Number of activities to pop from the tracked stack.
     */
    fun popCount(count: Int): StackNavigation {
        require(count >= 1) { "popCount must be at least 1" }
        popCount = count
        return this
    }

    /**
     * Navigate to another route after the pop operation completes.
     */
    fun target(url: String): StackNavigation {
        targetUrl = url
        return this
    }

    /**
     * Set a custom result code for the current top activity.
     */
    fun resultCode(code: Int): StackNavigation {
        resultCode = code
        return this
    }

    /**
     * Add a result payload that will be sent from the current top activity.
     */
    fun result(key: String, value: Any?): StackNavigation {
        when (value) {
            null -> resultExtras.putString(key, null)
            is String -> resultExtras.putString(key, value)
            is Int -> resultExtras.putInt(key, value)
            is Long -> resultExtras.putLong(key, value)
            is Float -> resultExtras.putFloat(key, value)
            is Double -> resultExtras.putDouble(key, value)
            is Boolean -> resultExtras.putBoolean(key, value)
            is Bundle -> resultExtras.putBundle(key, value)
            is CharSequence -> resultExtras.putCharSequence(key, value)
            is java.io.Serializable -> resultExtras.putSerializable(key, value)
            else -> throw IllegalArgumentException("Unsupported result type: ${value::class.java.name}")
        }
        return this
    }

    /**
     * Merge multiple result values into the returning payload.
     */
    fun result(bundle: Bundle): StackNavigation {
        resultExtras.putAll(bundle)
        return this
    }

    /**
     * Copy result values from an existing Intent.
     */
    fun result(intent: Intent): StackNavigation {
        intent.extras?.let(resultExtras::putAll)
        return this
    }

    /**
     * Merge multiple result values from a map.
     */
    fun results(values: Map<String, Any?>): StackNavigation {
        values.forEach { (key, value) -> result(key, value) }
        return this
    }

    /**
     * Finish tracked activities and optionally navigate to a new target.
     */
    fun start() {
        val tracker = ActivityStackTracker.from(context)
        val activities = tracker.peekTop(popCount)
        if (activities.isEmpty()) {
            targetUrl?.let { urlRouter.navigation(context, it).start() }
            return
        }

        activities.firstOrNull()?.let { topActivity ->
            if (!resultExtras.isEmpty) {
                topActivity.setResult(resultCode, Intent().apply { putExtras(resultExtras) })
            }
        }

        activities.forEach { activity ->
            if (!activity.isFinishing) {
                activity.finish()
            }
        }

        targetUrl?.let { route ->
            val launchContext = tracker.topActivityExcluding(activities) ?: context.applicationContext
            urlRouter.navigation(launchContext, route).start()
        }
    }
}

internal class ActivityStackTracker private constructor(application: Application) : Application.ActivityLifecycleCallbacks {
    private val activities = mutableListOf<Activity>()

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun peekTop(count: Int): List<Activity> {
        if (count <= 0) return emptyList()
        return synchronized(activities) {
            activities.takeLast(count).reversed()
        }
    }

    fun topActivityExcluding(excluded: Collection<Activity>): Activity? {
        return synchronized(activities) {
            activities.lastOrNull { it !in excluded && !it.isFinishing }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        synchronized(activities) {
            activities.remove(activity)
            activities.add(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
        synchronized(activities) {
            activities.remove(activity)
            activities.add(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        synchronized(activities) {
            activities.remove(activity)
        }
    }

    companion object {
        @Volatile
        private var instance: ActivityStackTracker? = null

        fun from(context: Context): ActivityStackTracker {
            return instance ?: synchronized(this) {
                instance ?: ActivityStackTracker(context.applicationContext as Application).also { instance = it }
            }
        }

        internal fun resetForTests() {
            instance = null
        }
    }
}
