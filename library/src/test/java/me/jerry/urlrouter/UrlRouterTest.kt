package me.jerry.urlrouter

import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
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
}
