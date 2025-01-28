package dev.jordond.connectivity.tools

import android.annotation.SuppressLint
import android.content.Context
import dev.jordond.connectivity.InternalConnectivityApi

/**
 * Class for providing the application context.
 */
@InternalConnectivityApi
public class ContextProvider(public val context: Context) {

    @InternalConnectivityApi
    public companion object {

        @SuppressLint("StaticFieldLeak")
        internal var instance: ContextProvider? = null

        /**
         * Create a new instance of [ContextProvider] with the given [context].
         *
         * @param context The application context.
         * @return The [ContextProvider] instance.
         */
        public fun create(context: Context): ContextProvider {
            if (instance == null) {
                instance = ContextProvider(context)
            }

            return instance!!
        }

        /**
         * Get the [ContextProvider] instance.
         *
         * @return The [ContextProvider] instance.
         */
        public fun getInstance(): ContextProvider = instance
            ?: throw IllegalStateException("ContextProvider has not been initialized")
    }
}