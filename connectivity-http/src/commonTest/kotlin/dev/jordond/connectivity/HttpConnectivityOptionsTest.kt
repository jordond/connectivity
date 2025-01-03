package dev.jordond.connectivity

import dev.jordond.connectivity.HttpConnectivityOptions.Companion.DEFAULT_HTTP_METHOD
import dev.jordond.connectivity.HttpConnectivityOptions.Companion.DEFAULT_POLLING_INTERVAL_MS
import dev.jordond.connectivity.HttpConnectivityOptions.Companion.DEFAULT_PORT
import dev.jordond.connectivity.HttpConnectivityOptions.Companion.DEFAULT_TIMEOUT
import dev.jordond.connectivity.HttpConnectivityOptions.Companion.DEFAULT_URLS
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.http.HttpMethod
import kotlin.test.Test

class HttpConnectivityOptionsTest {

    @Test
    fun verifyDefaultValues() {
        val options = HttpConnectivityOptions.build { }

        options.options.autoStart.shouldBeFalse()
        options.urls shouldBeEqual DEFAULT_URLS
        options.port shouldBeExactly DEFAULT_PORT
        options.method shouldBeEqual DEFAULT_HTTP_METHOD
        options.timeoutMs shouldBeExactly DEFAULT_TIMEOUT
        options.pollingIntervalMs shouldBeExactly DEFAULT_POLLING_INTERVAL_MS
        options.onPollResult.shouldBeNull()
    }

    @Test
    fun canSetAutoStart() {
        val options = HttpConnectivityOptions.build {
            autoStart = true
        }

        options.options.autoStart.shouldBeTrue()
    }

    @Test
    fun canAddSingleUrl() {
        val options = HttpConnectivityOptions.build {
            url("test.com")
        }

        options.urls shouldBeEqual listOf("test.com")
    }

    @Test
    fun canSetMultipleUrlsUsingVararg() {
        val options = HttpConnectivityOptions.build {
            urls("test1.com", "test2.com")
        }

        options.urls shouldBeEqual listOf("test1.com", "test2.com")
    }

    @Test
    fun canSetUrlsUsingList() {
        val urlList = listOf("test1.com", "test2.com", "test3.com")
        val options = HttpConnectivityOptions.build {
            urls(urlList)
        }

        options.urls shouldBeEqual urlList
    }

    @Test
    fun canSetPort() {
        val options = HttpConnectivityOptions.build {
            port = 8080
        }

        options.port shouldBeExactly 8080
    }

    @Test
    fun canSetHttpMethod() {
        val options = HttpConnectivityOptions.build {
            method = HttpMethod.Post
        }

        options.method shouldBeEqual HttpMethod.Post
    }

    @Test
    fun canSetTimeout() {
        val options = HttpConnectivityOptions.build {
            timeoutMs = 5000L
        }

        options.timeoutMs shouldBeExactly 5000L
    }

    @Test
    fun canSetPollingInterval() {
        val options = HttpConnectivityOptions.build {
            pollingIntervalMs = 10000L
        }

        options.pollingIntervalMs shouldBeExactly 10000L
    }

    @Test
    fun canSetOnPollResultCallback() {
        var callbackCalled = false
        val options = HttpConnectivityOptions.build {
            onPollResult { callbackCalled = true }
        }

        options.onPollResult.shouldNotBeNull()
        options.onPollResult?.invoke(PollResult.Error(Exception()))
        callbackCalled.shouldBeTrue()
    }

    @Test
    fun secondsExtensionConvertsCorrectly() {
        val options = HttpConnectivityOptions.build {
            timeoutMs = 5.seconds
        }

        options.timeoutMs shouldBeExactly 5000L
    }

    @Test
    fun minutesExtensionConvertsCorrectly() {
        val options = HttpConnectivityOptions.build {
            pollingIntervalMs = 2.minutes
        }

        options.pollingIntervalMs shouldBeExactly 120000L
    }

    @Test
    fun multipleConfigurationsAreAppliedCorrectly() {
        val options = HttpConnectivityOptions.build {
            autoStart = true
            url("test.com")
            port = 8080
            method = HttpMethod.Post
            timeoutMs = 5.seconds
            pollingIntervalMs = 1.minutes
        }

        options.options.autoStart.shouldBeTrue()
        options.urls shouldBeEqual listOf("test.com")
        options.port shouldBeExactly 8080
        options.method shouldBeEqual HttpMethod.Post
        options.timeoutMs shouldBeExactly 5000L
        options.pollingIntervalMs shouldBeExactly 60000L
    }

    @Test
    fun urlsAreClearedWhenSettingNewUrls() {
        val options = HttpConnectivityOptions.build {
            url("test1.com")
            urls("test2.com", "test3.com")
        }

        options.urls shouldBeEqual listOf("test2.com", "test3.com")
    }

    @Test
    fun httpExtensionFunctionBuilder() {
        val options = ConnectivityOptions.http {
            autoStart = true
            url("test.com")
            port = 8080
            method = HttpMethod.Post
            timeoutMs = 5.seconds
            pollingIntervalMs = 1.minutes
        }

        options.options.autoStart.shouldBeTrue()
        options.urls shouldBeEqual listOf("test.com")
        options.port shouldBeExactly 8080
        options.method shouldBeEqual HttpMethod.Post
        options.timeoutMs shouldBeExactly 5000L
        options.pollingIntervalMs shouldBeExactly 60000L
    }

}