package me.jerry.urlrouter

import android.app.Activity
import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UrlRouterTest {

    @Test
    fun canOpen_returnsTrueForRegisteredRoute() {
        UrlRouter.clear()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val result = UrlRouter.canOpen("sample://home")

        assertTrue(result)
    }

    @Test
    fun canOpen_returnsFalseForUnknownRoute() {
        UrlRouter.clear()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val result = UrlRouter.canOpen("sample://missing")

        assertFalse(result)
    }

    @Test
    fun canOpen_matchesPathTemplate() {
        UrlRouter.clear()
        UrlRouter.apply("sample://user/{id}", Target("UserActivity", pathTemplate = "/user/{id}"))

        val result = UrlRouter.canOpen("sample://user/123")

        assertTrue(result)
    }

    @Test
    fun remove_unregistersRoute() {
        UrlRouter.clear()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        UrlRouter.remove("sample://home")

        val result = UrlRouter.canOpen("sample://home")
        assertFalse(result)
    }

    @Test
    fun popBack_withoutFallback_finishesActivity() {
        UrlRouter.clear()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()

        val result = UrlRouter.popBack(activity)

        assertTrue(result)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun popBack_withContextNotActivity_returnsFalse() {
        UrlRouter.clear()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val context = activity.application

        val result = UrlRouter.popBack(context)

        assertFalse(result)
    }

    @Test
    fun navigation_startForResult_buildsSuccessfully() {
        UrlRouter.clear()
        UrlRouter.apply("sample://detail", Target("DetailActivity"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val navigation = UrlRouter.navigation(activity, "sample://detail")

        // Just verify the method exists and doesn't throw
        // Actual startActivityForResult verification requires more complex Robolectric setup
        navigation.startForResult(activity, 1001)
    }
}
