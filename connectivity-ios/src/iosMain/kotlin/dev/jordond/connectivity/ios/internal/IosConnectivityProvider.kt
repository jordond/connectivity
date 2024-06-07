package dev.jordond.connectivity.ios.internal

import dev.jordond.connectivity.ios.Connectivity
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.NetworkExtension.NWPath
import platform.NetworkExtension.NWPathStatus
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue

internal class IosConnectivityProvider : ConnectivityProvider {

    override fun monitor(): Flow<Connectivity.Status> {
        val monitor = nw_path_monitor_create()
        val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), flags = 0uL)

        return callbackFlow {
            nw_path_monitor_set_update_handler(monitor) { path ->
                val nwPath: NWPath? = path as? NWPath
                val status: NWPathStatus? = nwPath?.status()
                when {
                    status != null && status.toLong() == nw_path_status_satisfied.toLong() -> {
                        val isWifi = nw_path_uses_interface_type(path, nw_interface_type_wifi)
                        val isMetered = !isWifi && (path.isExpensive() || path.isConstrained())

                        trySend(Connectivity.Status.Connected(isMetered))
                    }
                    else -> Connectivity.Status.Disconnected
                }
            }

            nw_path_monitor_set_queue(monitor, queue)
            nw_path_monitor_start(monitor)

            awaitClose {
                nw_path_monitor_cancel(monitor)
            }
        }
    }
}