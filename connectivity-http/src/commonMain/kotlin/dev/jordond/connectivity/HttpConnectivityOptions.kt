package dev.jordond.connectivity

import dev.drewhamilton.poko.Poko
import io.ktor.http.HttpMethod

/**
 * The [HttpConnectivityOptions] class is used to configure the HTTP connectivity monitoring.
 *
 * @property options The [ConnectivityOptions] used to configure the connectivity monitoring.
 * Defaults to a new [ConnectivityOptions] instance.
 * @property urls The list of urls to use when checking for connection.
 * @property port The port to use for the HTTP requests.
 * @property method The [HttpMethod] to use for the HTTP requests.
 * @property timeoutMs The timeout for the HTTP requests in milliseconds.
 * @property pollingIntervalMs The interval between each poll in milliseconds.
 * @property onPollResult The lambda function to call when a poll is made.
 */
@Poko
public class HttpConnectivityOptions(
    public val options: ConnectivityOptions = ConnectivityOptions(),
    public val urls: List<String> = DEFAULT_URLS,
    public val port: Int = DEFAULT_PORT,
    public val method: HttpMethod = DEFAULT_HTTP_METHOD,
    public val timeoutMs: Long = DEFAULT_TIMEOUT,
    public val pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
    public val onPollResult: ((result: PollResult) -> Unit)? = null,
) {

    /**
     * The [Builder] class is used to build a new [HttpConnectivityOptions] instance using a
     * builder pattern.
     *
     * @property options The [ConnectivityOptions] used to configure the connectivity monitoring.
     * @property autoStart A Boolean indicating whether the [Connectivity] instance should
     * automatically start.
     * @property urls The list of urls to use when checking for connection.
     * @property port The port to use for the HTTP requests.
     * @property method The [HttpMethod] to use for the HTTP requests.
     * @property timeoutMs The timeout for the HTTP requests in milliseconds.
     * @property pollingIntervalMs The interval between each poll in milliseconds.
     */
    public class Builder internal constructor() {

        private var options: ConnectivityOptions = ConnectivityOptions()

        /**
         * A Boolean indicating whether the [Connectivity] instance should automatically start.
         */
        public var autoStart: Boolean
            get() = options.autoStart
            set(value) {
                options = ConnectivityOptions(autoStart = value)
            }

        private val urls = DEFAULT_URLS.toMutableList()

        private var onPoll: ((result: PollResult) -> Unit)? = null

        public var port: Int = DEFAULT_PORT

        public var method: HttpMethod = DEFAULT_HTTP_METHOD

        public var timeoutMs: Long = DEFAULT_TIMEOUT

        public var pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS

        /**
         * Converts an [Int] to seconds.
         */
        public val Int.seconds: Long
            get() = this * 1000L

        /**
         * Converts an [Int] to minutes.
         */
        public val Int.minutes: Long
            get() = this * 60.seconds

        /**
         * Adds a url to the list of urls to monitor.
         *
         * @param url The url to add.
         * @return The [Builder] instance.
         */
        public fun url(url: String): Builder = apply { urls(url) }

        /**
         * Sets the list of urls to monitor.
         *
         * @param urls The urls to set.
         * @return The [Builder] instance.
         */
        public fun urls(vararg urls: String): Builder = apply { urls(urls.toList()) }

        /**
         * Sets the list of urls to monitor.
         *
         * @param urls The urls to set.
         * @return The [Builder] instance.
         */
        public fun urls(urls: List<String>): Builder = apply {
            this.urls.clear()
            this.urls.addAll(urls)
        }

        /**
         * Sets the callback when a poll is made.
         *
         * @param block The lambda function to call when a poll is made.
         * @return The [Builder] instance.
         */
        public fun onPollResult(block: (result: PollResult) -> Unit): Builder = apply {
            onPoll = block
        }

        /**
         * Builds a new [HttpConnectivityOptions] instance.
         *
         * @return The new [HttpConnectivityOptions] instance.
         */
        public fun build(): HttpConnectivityOptions = HttpConnectivityOptions(
            options = options,
            urls = urls,
            port = port,
            method = method,
            timeoutMs = timeoutMs,
            pollingIntervalMs = pollingIntervalMs,
            onPollResult = onPoll,
        )
    }

    public companion object {

        internal const val DEFAULT_PORT = 443
        internal val DEFAULT_HTTP_METHOD = HttpMethod.Get
        internal const val DEFAULT_TIMEOUT = 2000L
        internal const val DEFAULT_POLLING_INTERVAL_MS = (60 * 1000L) * 5
        internal val DEFAULT_URLS = listOf("google.com", "github.com", "bing.com")

        /**
         * Builds a new [HttpConnectivityOptions] instance using a builder pattern.
         *
         * @param block The lambda function that configures the [Builder].
         * @return The new [HttpConnectivityOptions] instance.
         */
        public fun build(
            block: Builder.() -> Unit,
        ): HttpConnectivityOptions = Builder().apply(block).build()
    }
}

/**
 * Builds a new [HttpConnectivityOptions] instance using a builder pattern.
 *
 * @param block The lambda function that configures the [HttpConnectivityOptions.Builder].
 * @return The new [HttpConnectivityOptions] instance.
 */
public fun ConnectivityOptions.Companion.http(
    block: HttpConnectivityOptions.Builder.() -> Unit,
): HttpConnectivityOptions = HttpConnectivityOptions.build(block)
