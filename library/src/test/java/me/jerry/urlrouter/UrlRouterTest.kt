package me.jerry.urlrouter

import android.app.Activity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UrlRouterTest {

    private fun resetRouterState() {
        UrlRouter.clear()
        ActivityStackTracker.resetForTests()
        UrlRouter.configuration().reset()
    }

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

    @Test
    fun navigation_putExtrasMap_mergesCrossPagePayload() {
        resetRouterState()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val nested = android.os.Bundle().apply { putString("source", "feed") }

        val intent = UrlRouter.navigation(activity, "sample://home")
            .putExtras(
                mapOf(
                    "message" to "hello",
                    "count" to 3,
                    "meta" to nested,
                    "trace" to "route-1" as CharSequence
                )
            )
            .getIntent()

        assertNotNull(intent)
        assertEquals("hello", intent?.getStringExtra("message"))
        assertEquals(3, intent?.getIntExtra("count", -1))
        assertEquals("feed", intent?.getBundleExtra("meta")?.getString("source"))
        assertEquals("route-1", intent?.getCharSequenceExtra("trace"))
    }

    @Test
    fun navigation_putExtra_throwsForUnsupportedType() {
        resetRouterState()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()

        assertThrows(IllegalArgumentException::class.java) {
            UrlRouter.navigation(activity, "sample://home")
                .putExtra("bad", object {})
        }
    }

    @Test
    fun stack_popCount_finishesRequestedActivities() {
        resetRouterState()
        val first = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        ActivityStackTracker.from(first)
        val second = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        val third = Robolectric.buildActivity(Activity::class.java).create().resume().get()

        UrlRouter.stack(third)
            .popCount(2)
            .start()

        assertFalse(first.isFinishing)
        assertTrue(second.isFinishing)
        assertTrue(third.isFinishing)
    }

    @Test
    fun stack_withResult_andTarget_popsThenNavigates() {
        resetRouterState()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val first = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        ActivityStackTracker.from(first)
        val second = Robolectric.buildActivity(Activity::class.java).create().resume().get()

        UrlRouter.stack(second)
            .popCount(1)
            .resultCode(Activity.RESULT_CANCELED)
            .result("status", "cancelled")
            .target("sample://home?from=stack")
            .start()

        val shadow = shadowOf(second)
        assertEquals(Activity.RESULT_CANCELED, shadow.resultCode)
        val resultIntent = shadow.resultIntent
        assertNotNull(resultIntent)
        assertEquals("cancelled", resultIntent.getStringExtra("status"))

        // Verify navigation happened from first activity
        val nextIntent = shadowOf(first).nextStartedActivity
        assertNotNull(nextIntent)
        assertEquals("app://home?from=stack", nextIntent?.dataString)
    }

    @Test
    fun stack_target_navigatesAfterPop() {
        resetRouterState()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val first = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        ActivityStackTracker.from(first)
        val second = Robolectric.buildActivity(Activity::class.java).create().resume().get()

        UrlRouter.stack(second)
            .target("sample://home?from=stack")
            .start()

        val nextIntent = shadowOf(first).nextStartedActivity
        assertTrue(second.isFinishing)
        assertNotNull(nextIntent)
        assertEquals("app://home?from=stack", nextIntent?.dataString)
    }

    @Test
    fun stack_results_mergesBundleIntentAndMapIntoResultIntent() {
        resetRouterState()
        val first = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        ActivityStackTracker.from(first)
        val second = Robolectric.buildActivity(Activity::class.java).create().resume().get()

        val bundle = android.os.Bundle().apply {
            putString("from", "bundle")
            putInt("step", 2)
        }
        val sourceIntent = android.content.Intent().apply {
            putExtra("token", "abc")
        }

        UrlRouter.stack(second)
            .result(bundle)
            .result(sourceIntent)
            .results(mapOf("status" to "done", "trace" to "stack-1" as CharSequence))
            .start()

        val resultIntent = shadowOf(second).resultIntent
        assertNotNull(resultIntent)
        assertEquals("bundle", resultIntent?.getStringExtra("from"))
        assertEquals(2, resultIntent?.getIntExtra("step", -1))
        assertEquals("abc", resultIntent?.getStringExtra("token"))
        assertEquals("done", resultIntent?.getStringExtra("status"))
        assertEquals("stack-1", resultIntent?.getCharSequenceExtra("trace"))
    }

    @Test
    fun routeAspect_observesResolvedNavigationLifecycle() {
        resetRouterState()
        UrlRouter.apply("sample://home", Target("HomeActivity"))

        val events = mutableListOf<String>()
        UrlRouter.configuration().addRouteAspect(object : RouteAspect {
            override fun onNavigationStart(originalUri: android.net.Uri) {
                events += "start:${originalUri}"
            }

            override fun onRouteResolved(originalUri: android.net.Uri, resolvedUri: android.net.Uri, target: Target) {
                events += "resolved:${resolvedUri}"
            }

            override fun onIntentCreated(originalUri: android.net.Uri, intent: android.content.Intent) {
                events += "intent:${intent.dataString}"
            }

            override fun onNavigationComplete(originalUri: android.net.Uri, intent: android.content.Intent) {
                events += "complete:${intent.dataString}"
            }
        })

        val activity = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        UrlRouter.navigation(activity, "sample://home?from=aop").start()

        assertEquals(
            listOf(
                "start:sample://home?from=aop",
                "resolved:app://home?from=aop",
                "intent:app://home?from=aop",
                "complete:app://home?from=aop"
            ),
            events
        )
    }

    @Test
    fun routeAspect_observesRequestInterceptAndNotFound() {
        resetRouterState()

        val events = mutableListOf<String>()
        UrlRouter.configuration()
            .addRequestInterceptor(object : RequestInterceptor {
                override fun intercept(uri: android.net.Uri): Boolean = uri.host == "blocked"
            })
            .addRouteAspect(object : RouteAspect {
                override fun onNavigationStart(originalUri: android.net.Uri) {
                    events += "start:${originalUri}"
                }

                override fun onRequestIntercepted(originalUri: android.net.Uri) {
                    events += "request:${originalUri}"
                }

                override fun onTargetNotFound(originalUri: android.net.Uri) {
                    events += "notfound:${originalUri}"
                }
            })

        val activity = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        UrlRouter.navigation(activity, "sample://blocked").start()
        UrlRouter.navigation(activity, "sample://missing").start()

        assertEquals(
            listOf(
                "start:sample://blocked",
                "request:sample://blocked",
                "start:sample://missing",
                "notfound:sample://missing"
            ),
            events
        )
    }

}
