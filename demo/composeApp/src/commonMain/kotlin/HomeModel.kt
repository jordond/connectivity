import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.jordond.connectivity.Connectivity
import dev.stateholder.extensions.voyager.StateScreenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeModel : StateScreenModel<HomeModel.State>(State()) {

    private val connectivity = platformConnectivity()

    init {
        screenModelScope.launch(Dispatchers.Default) {
            connectivity.monitoring.collect { isMonitoring ->
                Logger.i { "Connectivity isMonitoring: $isMonitoring" }
                updateState { state ->
                    state.copy(monitoring = isMonitoring)
                }
            }
        }

        screenModelScope.launch(Dispatchers.Default) {
            connectivity.statusUpdates.collect { update ->
                Logger.i { "Connectivity update: $update" }
                updateState { state ->
                    state.copy(status = update)
                }
            }
        }
    }

    fun toggle() {
        if (state.value.monitoring) stop() else start()
    }

    private fun start() {
        connectivity.start()
    }

    private fun stop() {
        connectivity.stop()
    }

    data class State(
        val status: Connectivity.Status? = null,
        val monitoring: Boolean = false,
    )
}