import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.jordond.connectivity.Connectivity
import dev.stateholder.extensions.voyager.StateScreenModel
import kotlinx.coroutines.launch

class HomeModel : StateScreenModel<HomeModel.State>(State()) {

    private val connectivity = platformConnectivity()

    init {
        screenModelScope.launch {
            connectivity.status.collect { status ->
                if (state.value.active) {
                    Logger.i { "Connectivity status: $status" }
                    updateState { it.copy(status = status) }
                }
            }
        }
    }

    fun toggle() {
        if (state.value.active) stop() else start()
    }

    private fun start() {
        connectivity.start()
        updateState { it.copy(active = true) }
    }

    private fun stop() {
        connectivity.stop()
        updateState { it.copy(active = false) }
    }

    data class State(
        val status: Connectivity.Status? = null,
        val active: Boolean = false,
    )
}