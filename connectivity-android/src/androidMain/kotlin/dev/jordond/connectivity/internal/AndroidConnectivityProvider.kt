package dev.jordond.connectivity.internal

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

internal class AndroidConnectivityProvider(
    private val context: Context,
) : ConnectivityProvider {

    // The permission is in the manifest but the IDE doesn't seem to recognize it
    @SuppressLint("MissingPermission")
    override fun monitor(): Flow<Connectivity.Status> {
        val manager = context.getSystemService<ConnectivityManager>()
            ?: return flowOf(Connectivity.Status.Disconnected)

        return callbackFlow {
            val networkRequest = NetworkRequest.Builder().build()

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val capabilities = manager.getNetworkCapabilities(network)
                    val status = manager.status(capabilities)
                    trySend(status)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    val status = manager.status(networkCapabilities)
                    trySend(status)
                }

                override fun onLost(network: Network) {
                    trySend(Connectivity.Status.Disconnected)
                }
            }

            manager.registerNetworkCallback(networkRequest, networkCallback)

            awaitClose { manager.unregisterNetworkCallback(networkCallback) }
        }
    }
}

@SuppressLint("MissingPermission")
private fun ConnectivityManager.status(
    capabilities: NetworkCapabilities?,
): Connectivity.Status.Connected {
    val isWifi = capabilities?.hasTransport(TRANSPORT_WIFI) ?: false
    val isCellular = capabilities?.hasTransport(TRANSPORT_CELLULAR) ?: false
    val isMetered = !isWifi || isCellular || isActiveNetworkMetered
    return Connectivity.Status.Connected(isMetered)
}