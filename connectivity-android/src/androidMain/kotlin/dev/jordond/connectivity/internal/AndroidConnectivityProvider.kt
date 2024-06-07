package dev.jordond.connectivity.internal

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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

    @SuppressLint("MissingPermission")
    override fun monitor(): Flow<Connectivity.Status> {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
            ?: return flowOf(Connectivity.Status.Disconnected)

        return callbackFlow {
            val networkRequest = NetworkRequest.Builder().build()

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val capability = connectivityManager.getNetworkCapabilities(network)
                    val isWifi = capability?.hasTransport(TRANSPORT_WIFI) ?: false
                    val isCellular = capability?.hasTransport(TRANSPORT_CELLULAR) ?: false
                    val isMetered =
                        !isWifi || isCellular || connectivityManager.isActiveNetworkMetered

                    val status = Connectivity.Status.Connected(isMetered)
                    trySend(status)
                }

                override fun onLost(network: Network) {
                    trySend(Connectivity.Status.Disconnected)
                }
            }

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

            awaitClose { connectivityManager.unregisterNetworkCallback(networkCallback) }
        }
    }
}