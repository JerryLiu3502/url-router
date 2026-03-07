package me.jerry.urlrouter

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultIntentHandlerTest {

    private lateinit var context: Context
    private val handler = DefaultIntentHandler()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun createIntent_copiesSingleQueryParameterIntoExtras() {
        val intent = handler.createIntent(
            context = context,
            target = Target("me.jerry.urlrouter.TargetActivity"),
            uri = Uri.parse("sample://target?from=router")
        )

        assertEquals("router", intent.getStringExtra("from"))
        assertEquals("sample://target?from=router", intent.dataString)
    }

    @Test
    fun createIntent_copiesRepeatedQueryParametersIntoStringListExtra() {
        val intent = handler.createIntent(
            context = context,
            target = Target("me.jerry.urlrouter.TargetActivity"),
            uri = Uri.parse("sample://target?tag=one&tag=two")
        )

        val values = intent.getStringArrayListExtra("tag")
        assertNotNull(values)
        assertEquals(listOf("one", "two"), values)
    }
}
