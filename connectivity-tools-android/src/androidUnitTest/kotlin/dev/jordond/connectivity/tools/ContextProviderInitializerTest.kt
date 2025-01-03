package dev.jordond.connectivity.tools

import android.content.Context
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlin.test.Test

class ContextProviderInitializerTest {

    @Test
    fun shouldCreateContextProvider() {
        val context = mockk<Context>()
        val initializer = ContextProviderInitializer()

        val result = initializer.create(context)

        result.shouldBeInstanceOf<ContextProvider>()
        result.context shouldBe context
    }

    @Test
    fun shouldReturnEmptyDependencyList() {
        val initializer = ContextProviderInitializer()

        val dependencies = initializer.dependencies()

        dependencies shouldBe emptyList()
    }
}