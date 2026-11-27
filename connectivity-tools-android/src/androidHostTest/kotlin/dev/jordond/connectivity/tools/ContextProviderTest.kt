package dev.jordond.connectivity.tools

import android.content.Context
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContextProviderTest {

    private val context: Context = mockk()

    @BeforeTest
    fun setup() {
        ContextProvider.instance = null
    }

    @AfterTest
    fun cleanup() {
        ContextProvider.instance = null
    }

    @Test
    fun shouldThrowWhenNotInitialized() {
        val error = shouldThrow<IllegalStateException> {
            ContextProvider.getInstance()
        }

        error.message shouldBe "ContextProvider has not been initialized"
    }

    @Test
    fun shouldCreateInstanceWithContext() {
        val provider = ContextProvider.create(context)

        provider.context shouldBeSameInstanceAs context
        ContextProvider.getInstance() shouldBeSameInstanceAs provider
    }

    @Test
    fun shouldKeepFirstInstanceWhenCreatedAgain() {
        val first = ContextProvider.create(context)
        val second = ContextProvider.create(mockk())

        second shouldBeSameInstanceAs first
        second.context shouldBeSameInstanceAs context
    }

    @Test
    fun shouldCreateInstanceFromInitializer() {
        val initializer = ContextProviderInitializer()

        val provider = initializer.create(context)

        provider.context shouldBeSameInstanceAs context
        ContextProvider.getInstance() shouldBeSameInstanceAs provider
        initializer.dependencies().shouldBeEmpty()
    }
}
