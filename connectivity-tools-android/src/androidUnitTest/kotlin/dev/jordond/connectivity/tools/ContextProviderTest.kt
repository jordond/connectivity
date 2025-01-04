package dev.jordond.connectivity.tools

import android.content.Context
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test

class ContextProviderTest {

    @AfterTest
    fun cleanup() {
        ContextProvider.Companion.instance = null
    }

    @Test
    fun shouldCreateContextProviderInstance() {
        val context = mockk<Context>()
        val provider = ContextProvider.create(context)

        provider.shouldNotBeNull()
        provider.shouldBeInstanceOf<ContextProvider>()
        provider.context shouldBeSameInstanceAs context
    }

    @Test
    fun shouldReturnExistingInstanceWhenCreatingMultipleTimes() {
        val context1 = mockk<Context>()
        val context2 = mockk<Context>()

        val provider1 = ContextProvider.create(context1)
        val provider2 = ContextProvider.create(context2)

        provider1 shouldBe provider2
        provider1.context shouldBe context1
        provider2.context shouldBe context1
    }

    @Test
    fun shouldGetInstanceAfterCreation() {
        val context = mockk<Context>()
        val created = ContextProvider.create(context)
        val instance = ContextProvider.getInstance()

        instance shouldBe created
        instance.context shouldBe context
    }

    @Test
    fun shouldThrowWhenGettingInstanceBeforeCreation() {
        shouldThrow<IllegalStateException> {
            ContextProvider.getInstance()
        }
    }
}