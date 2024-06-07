package dev.jordondev.connectivity

import dev.drewhamilton.poko.Poko
import dev.jordond.connectivity.ConnectivityOptions
import io.ktor.http.HttpMethod

@Poko
public class HttpConnectivityOptions(
    public val options: ConnectivityOptions = ConnectivityOptions(),
    public val hosts: List<String> = DEFAULT_HOSTS,
    public val port: Int = DEFAULT_PORT,
    public val method: HttpMethod = DEFAULT_HTTP_METHOD,
    public val timeout: Long = DEFAULT_TIMEOUT,
    public val pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
) {

    public class Builder internal constructor() {

        private var options: ConnectivityOptions = ConnectivityOptions()

        public var autoStart: Boolean
            get() = options.autoStart
            set(value) {
                options = ConnectivityOptions(autoStart = value)
            }

        private val hosts = mutableListOf<String>()

        public var port: Int = DEFAULT_PORT

        public var method: HttpMethod = DEFAULT_HTTP_METHOD

        public var timeout: Long = DEFAULT_TIMEOUT

        public var pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS

        public fun host(host: String): Builder = apply { hosts(host) }

        public fun hosts(vararg hosts: String): Builder = apply { hosts(hosts.toList()) }

        public fun hosts(hosts: List<String>): Builder = apply {
            this.hosts.clear()
            this.hosts.addAll(hosts)
        }

        public fun build(): HttpConnectivityOptions = HttpConnectivityOptions(
            options = options,
            hosts = hosts,
            port = port,
            timeout = timeout,
            pollingIntervalMs = pollingIntervalMs,
        )
    }

    public companion object {

        private const val DEFAULT_PORT = 443
        private val DEFAULT_HTTP_METHOD = HttpMethod.Get
        private const val DEFAULT_TIMEOUT = 5000L
        private const val DEFAULT_POLLING_INTERVAL_MS = 5000L
        private val DEFAULT_HOSTS = listOf(
            "https://google.com",
            "https://github.com",
            "https://bing.com",
        )

        public fun build(
            block: Builder.() -> Unit,
        ): HttpConnectivityOptions = Builder().apply(block).build()
    }
}

public fun ConnectivityOptions.Companion.http(
    block: HttpConnectivityOptions.Builder.() -> Unit,
): HttpConnectivityOptions {
    return HttpConnectivityOptions.build(block)
}
