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
        private var instance: ContextProvider? = null

        public fun create(context: Context): ContextProvider {
            if (instance == null) {
                instance = ContextProvider(context)
            }

            return instance!!
        }

        public fun getInstance(): ContextProvider = instance
            ?: throw IllegalStateException("ContextProvider has not been initialized")
    }
}