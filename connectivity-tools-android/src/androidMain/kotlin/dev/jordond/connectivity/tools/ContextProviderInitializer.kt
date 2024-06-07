package dev.jordond.connectivity.tools

import android.content.Context
import androidx.startup.Initializer
import dev.jordond.connectivity.InternalConnectivityApi

@InternalConnectivityApi
internal class ContextProviderInitializer : Initializer<ContextProvider> {

    override fun create(context: Context): ContextProvider {
        return ContextProvider.create(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
