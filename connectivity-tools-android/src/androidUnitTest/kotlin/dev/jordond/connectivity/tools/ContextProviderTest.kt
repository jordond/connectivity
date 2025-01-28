package dev.jordond.connectivity.tools

import android.content.Context
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test

// The tests seem to fail if we just compare the instances directly, so we need to compare the properties
class ContextProviderTest {

    @AfterTest
    fun cleanup() {
        ContextProvider.Companion.instance = null
    }

    @Test
    fun shouldReturnExistingInstanceWhenCreatingMultipleTimes() {
        val context1 = mockk<Context> {
            every { packageName } returns "com.example"
        }
        val context2 = mockk<Context> {
            every { packageName } returns "com.other"
        }

        val provider1 = ContextProvider.create(context1)
        val provider2 = ContextProvider.create(context2)

        provider1 shouldBe provider2
        provider1.context.packageName shouldBeEqual "com.example"
        provider2.context.packageName shouldBeEqual "com.example"
    }

    @Test
    fun shouldGetInstanceAfterCreation() {
        val context = mockk<Context> {
            every { packageName } returns "com.example"
        }
        val created = ContextProvider.create(context)
        val instance = ContextProvider.getInstance()

        instance shouldBe created
        instance.context.packageName shouldBeEqual "com.example"
    }

    @Test
    fun shouldThrowWhenGettingInstanceBeforeCreation() {
        shouldThrow<IllegalStateException> {
            ContextProvider.getInstance()
        }
    }
}