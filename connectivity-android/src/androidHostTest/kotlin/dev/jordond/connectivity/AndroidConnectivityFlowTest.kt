package dev.jordond.connectivity

import android.content.Context
import android.net.ConnectivityManager
import dev.jordond.connectivity.tools.ContextProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AndroidConnectivityFlowTest {

    private lateinit var contextProvider: ContextProvider

    @BeforeTest
    fun setup() {
        contextProvider = mockk()
        mockkObject(ContextProvider.Companion)
        every { ContextProvider.getInstance() } returns contextProvider
        every { contextProvider.context } returns mockk {
            // No ConnectivityManager, so the provider emits Disconnected without touching the
            // Android framework, which isn't available in a host test.
            every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns null
            every { getSystemService(ConnectivityManager::class.java) } returns null
        }
    }

    @AfterTest
    fun cleanup() {
        unmockkObject(ContextProvider.Companion)
    }

    @Test
    fun emitsDisconnectedWhenNoConnectivityManager() = runTest {
        val status = androidConnectivityFlow().first()

        status.shouldBeInstanceOf<Connectivity.Status.Disconnected>()
    }

    @Test
    fun doesNotResolveContextUntilCollected() = runTest {
        // Undo the setup() mock so ContextProvider is genuinely uninitialized for this test.
        unmockkObject(ContextProvider.Companion)

        val flow = androidConnectivityFlow()

        shouldThrow<IllegalStateException> {
            flow.first()
        }
    }
}
