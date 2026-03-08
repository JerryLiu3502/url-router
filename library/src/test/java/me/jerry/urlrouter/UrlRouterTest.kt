package me.jerry.urlrouter

import android.app.Activity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun canOpen_matchesRouteWhenIncomingUrlOmitsScheme() {
        UrlRouter.clear()
        UrlRouter.apply("sample://profile/details", Target("DetailsActivity"))

        val result = UrlRouter.canOpen("profile/details")

        assertTrue(result)
    }

    @Test
    fun canOpen_followsRedirectToFinalActivityTarget() {
        UrlRouter.clear()
        UrlRouter.apply("sample://legacy", Target.redirect("sample://home"))
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val result = UrlRouter.canOpen("legacy")

        assertTrue(result)
    }

    @Test
    fun canOpen_returnsFalseForBrokenRedirectTarget() {
        UrlRouter.clear()
        UrlRouter.apply("sample://legacy", Target.redirect("sample://missing"))

        val result = UrlRouter.canOpen("sample://legacy")

        assertFalse(result)
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

    @Test
    fun navigation_hasTargetAndGetIntent_resolveRedirectedRoute() {
        UrlRouter.clear()
        UrlRouter.apply("sample://legacy/{id}", Target.redirect("sample://user/{id}"))
        UrlRouter.apply("sample://user/{id}", Target("UserActivity", pathTemplate = "/user/{id}"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val navigation = UrlRouter.navigation(activity, "legacy/42?from=feed")

        assertTrue(navigation.hasTarget())

        val intent = navigation.getIntent()

        assertNotNull(intent)
        assertEquals("app://user/42?from=feed", intent?.dataString)
        assertEquals("42", intent?.getStringExtra("id"))
        assertEquals("feed", intent?.getStringExtra("from"))
    }

    @Test
    fun navigation_getIntentReturnsNullWhenNoFinalTargetExists() {
        UrlRouter.clear()
        UrlRouter.apply("sample://legacy", Target.redirect("sample://missing"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val navigation = UrlRouter.navigation(activity, "legacy")

        assertFalse(navigation.hasTarget())
        assertNull(navigation.getIntent())
    }


    @Test
    fun navigation_ifIntentNonNullSendTo_invokesReceiverWhenResolved() {
        UrlRouter.clear()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val navigation = UrlRouter.navigation(activity, "sample://home?from=test")

        var captured: android.content.Intent? = null
        navigation.ifIntentNonNullSendTo(IntentReceiver { captured = it })

        assertNotNull(captured)
        assertEquals("test", captured?.getStringExtra("from"))
    }

    @Test
    fun navigation_ifIntentNonNullSendTo_skipsReceiverWhenUnresolved() {
        UrlRouter.clear()

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val navigation = UrlRouter.navigation(activity, "sample://missing")

        var called = false
        navigation.ifIntentNonNullSendTo(IntentReceiver { called = true })

        assertFalse(called)
    }



    @Test
    fun navigation_putExtrasIntent_mergesIntentExtrasIntoResolvedIntent() {
        UrlRouter.clear()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val sourceIntent = android.content.Intent().apply {
            putExtra("userId", 7)
            putExtra("from", "intent")
        }

        val intent = UrlRouter.navigation(activity, "sample://home")
            .putExtras(sourceIntent)
            .getIntent()

        assertNotNull(intent)
        assertEquals(7, intent?.getIntExtra("userId", -1))
        assertEquals("intent", intent?.getStringExtra("from"))
    }



    @Test
    fun navigation_appendQueryParameters_appendsAllPairs() {
        UrlRouter.clear()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val intent = UrlRouter.navigation(activity, "sample://home")
            .appendQueryParameters(mapOf("from" to "feed", "tab" to "hot"))
            .getIntent()

        assertNotNull(intent)
        assertEquals("feed", intent?.getStringExtra("from"))
        assertEquals("hot", intent?.getStringExtra("tab"))
    }

}
