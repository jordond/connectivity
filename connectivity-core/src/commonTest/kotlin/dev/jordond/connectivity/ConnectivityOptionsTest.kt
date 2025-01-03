package dev.jordond.connectivity

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ConnectivityOptionsTest {

    @Test
    fun verifyDefaultValues() {
        val options = ConnectivityOptions.build { }

        options.autoStart.shouldBeFalse()
    }

    @Test
    fun canSetAutoStart() {
        val options = ConnectivityOptions.build {
            autoStart = true
        }

        options.autoStart.shouldBeTrue()
    }

    @Test
    fun canSetAutoStartUsingBuilderFunction() {
        val options = ConnectivityOptions.build {
            autoStart(true)
        }

        options.autoStart.shouldBeTrue()
    }

    @Test
    fun multipleConfigurationsAreAppliedCorrectly() {
        val options = ConnectivityOptions.build {
            autoStart = true
            autoStart(false)
        }

        options.autoStart.shouldBeFalse()
    }

    @Test
    fun defaultConstructorHasCorrectValues() {
        val options = ConnectivityOptions()

        options.autoStart.shouldBeFalse()
    }

    @Test
    fun constructorWithParametersHasCorrectValues() {
        val options = ConnectivityOptions(autoStart = true)

        options.autoStart.shouldBeTrue()
    }
}