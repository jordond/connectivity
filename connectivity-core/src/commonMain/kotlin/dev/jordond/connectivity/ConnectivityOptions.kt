package dev.jordond.connectivity

import dev.drewhamilton.poko.Poko

/**
 * The [ConnectivityOptions] class provides configuration options for the [Connectivity] instance.
 *
 * @property autoStart A Boolean indicating whether the [Connectivity] instance should automatically
 * start monitoring connectivity status.
 *
 * @constructor Creates a new [ConnectivityOptions] instance.
 * @param autoStart A Boolean indicating whether the [Connectivity] instance should automatically
 * start monitoring connectivity status. Defaults to [DEFAULT_AUTO_START].
 */
@Poko
public open class ConnectivityOptions(
    public val autoStart: Boolean = DEFAULT_AUTO_START,
) {

    /**
     * The Builder class provides a way to construct a [ConnectivityOptions] instance using a
     * builder pattern.
     *
     * @property autoStart A Boolean indicating whether the [Connectivity] instance should
     * automatically start monitoring connectivity status.
     */
    public class Builder internal constructor() {

        private var autoStart: Boolean = DEFAULT_AUTO_START

        /**
         * Sets the autoStart property of the Builder.
         *
         * @param autoStart A Boolean indicating whether the [Connectivity] instance should
         * automatically start monitoring connectivity status.
         * @return The Builder instance with the autoStart property set.
         */
        public fun autoStart(autoStart: Boolean): Builder = apply { this.autoStart = autoStart }

        /**
         * Builds a [ConnectivityOptions] instance using the properties set on the Builder.
         *
         * @return A [ConnectivityOptions] instance.
         */
        public fun build(): ConnectivityOptions = ConnectivityOptions(autoStart)
    }

    /**
     * The companion object for the [ConnectivityOptions] class.
     *
     * @property DEFAULT_AUTO_START The default value for the autoStart property.
     */
    public companion object {

        private const val DEFAULT_AUTO_START: Boolean = true

        /**
         * Builds a [ConnectivityOptions] instance using a builder pattern.
         *
         * @param block A lambda function that configures the Builder.
         * @return A [ConnectivityOptions] instance.
         */
        public fun build(
            block: Builder.() -> Unit,
        ): ConnectivityOptions = Builder().apply(block).build()
    }
}
