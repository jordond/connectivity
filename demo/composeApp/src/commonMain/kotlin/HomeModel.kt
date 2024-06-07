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
            connectivity.updates.collect { update ->
                Logger.i { "Connectivity update: $update" }
                updateState { state ->
                    state.copy(
                        active = update.isActive,
                        status = if (update.isActive) update.status else state.status,
                    )
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