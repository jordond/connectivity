package dev.jordondev.connectivity

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityOptions
import io.ktor.http.HttpMethod

/**
 * The [HttpConnectivityOptions] class is used to configure the HTTP connectivity monitoring.
 *
 * @property options The [ConnectivityOptions] used to configure the connectivity monitoring.
 * Defaults to a new [ConnectivityOptions] instance.
 * @property hosts The list of hosts to use when checking for connection.
 * @property port The port to use for the HTTP requests..
 * @property method The [HttpMethod] to use for the HTTP requests.
 * @property timeoutMs The timeout for the HTTP requests in milliseconds.
 * @property pollingIntervalMs The interval between each poll in milliseconds.
 */
@Poko
public class HttpConnectivityOptions(
    public val options: ConnectivityOptions = ConnectivityOptions(),
    public val hosts: List<String> = DEFAULT_HOSTS,
    public val port: Int = DEFAULT_PORT,
    public val method: HttpMethod = DEFAULT_HTTP_METHOD,
    public val timeoutMs: Long = DEFAULT_TIMEOUT,
    public val pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
) {

    /**
     * The [Builder] class is used to build a new [HttpConnectivityOptions] instance using a
     * builder pattern.
     *
     * @property options The [ConnectivityOptions] used to configure the connectivity monitoring.
     * @property autoStart A Boolean indicating whether the [Connectivity] instance should
     * automatically start.
     * @property hosts The list of hosts to use when checking for connection.
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

        private val hosts = mutableListOf<String>()

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
         * Adds a host to the list of hosts to monitor.
         *
         * @param host The host to add.
         * @return The [Builder] instance.
         */
        public fun host(host: String): Builder = apply { hosts(host) }

        /**
         * Sets the list of hosts to monitor.
         *
         * @param hosts The hosts to set.
         * @return The [Builder] instance.
         */
        public fun hosts(vararg hosts: String): Builder = apply { hosts(hosts.toList()) }

        /**
         * Sets the list of hosts to monitor.
         *
         * @param hosts The hosts to set.
         * @return The [Builder] instance.
         */
        public fun hosts(hosts: List<String>): Builder = apply {
            this.hosts.clear()
            this.hosts.addAll(hosts)
        }

        /**
         * Builds a new [HttpConnectivityOptions] instance.
         *
         * @return The new [HttpConnectivityOptions] instance.
         */
        public fun build(): HttpConnectivityOptions = HttpConnectivityOptions(
            options = options,
            hosts = hosts,
            port = port,
            timeoutMs = timeoutMs,
            pollingIntervalMs = pollingIntervalMs,
        )
    }

    public companion object {

        private const val DEFAULT_PORT = 443
        private val DEFAULT_HTTP_METHOD = HttpMethod.Get
        private const val DEFAULT_TIMEOUT = 5000L
        private const val DEFAULT_POLLING_INTERVAL_MS = (60 * 1000L) * 5
        private val DEFAULT_HOSTS = listOf(
            "https://google.com",
            "https://github.com",
            "https://bing.com",
        )

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
