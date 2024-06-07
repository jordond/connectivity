package dev.jordond.connectivity

import dev.drewhamilton.poko.Poko

/**
 * Need to have:
 *
 * - Host
 * - Port
 * - Timeout
 * - How many seconds to poll
 * - A way to force a poll
 */

@Poko
public open class ConnectivityOptions(
    public val autoStart: Boolean = DEFAULT_AUTO_START,
) {

    public class Builder internal constructor() {

        private var autoStart: Boolean = DEFAULT_AUTO_START

        public fun autoStart(autoStart: Boolean): Builder = apply { this.autoStart = autoStart }

        public fun build(): ConnectivityOptions = ConnectivityOptions(autoStart)
    }

    public companion object {

        private const val DEFAULT_AUTO_START: Boolean = true

        public fun build(
            block: Builder.() -> Unit,
        ): ConnectivityOptions = Builder().apply(block).build()
    }
}
