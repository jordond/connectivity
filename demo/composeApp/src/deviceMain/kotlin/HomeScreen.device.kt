import androidx.compose.runtime.Composable
import dev.jordond.connectivity.compose.ConnectivityState
import dev.jordond.connectivity.compose.rememberConnectivityState

@Composable
internal actual fun createConnectivityState(): ConnectivityState {
    return rememberConnectivityState {
        autoStart = true
    }
}