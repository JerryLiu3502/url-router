package me.jerry.urlrouter

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TargetMapTest {

    @Test
    fun find_matchesFullRouteBeforeHostOnlyFallback() {
        val map = TargetMap()
        val homeTarget = Target("HomeActivity")
        val detailsTarget = Target("DetailsActivity")

        map.add("sample://profile", homeTarget)
        map.add("sample://profile/details", detailsTarget)

        val resolved = map.find(Uri.parse("sample://profile/details?id=1"))

        assertEquals(detailsTarget, resolved)
    }

    @Test
    fun find_returnsNullForUnknownRoute() {
        val map = TargetMap()
        map.add("sample://home", Target("HomeActivity"))

        val resolved = map.find(Uri.parse("sample://missing"))

        assertNull(resolved)
    }
}
