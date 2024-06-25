import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.compose.ConnectivityState

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val model = rememberScreenModel { HomeModel() }
        val state by model.state.collectAsState()

        MaterialTheme {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                ConnectivityContent(
                    title = "Manual Connectivity",
                    status = state.status,
                    isMonitoring = state.monitoring,
                    onToggle = model::toggle,
                )

                Card {
                    val connectivityState = createConnectivityState(false)
                    ConnectivityContent(
                        title = "Compose Connectivity",
                        status = connectivityState.status,
                        isMonitoring = connectivityState.isMonitoring,
                        onToggle = {
                            if (connectivityState.isMonitoring) {
                                connectivityState.stopMonitoring()
                            } else {
                                connectivityState.startMonitoring()
                            }
                        },
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal expect fun createConnectivityState(autoStart: Boolean = true): ConnectivityState

@Composable
private fun ConnectivityContent(
    title: String,
    status: Connectivity.Status?,
    isMonitoring: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(title)
        Crossfade(status) { status ->
            if (status == null) {
                Text("No status yet")
            } else {
                val text = if (!status.isConnected) "Disconnected"
                else "Connected${if (status.isMetered) " - Metered" else ""}"

                Text("Status: $text")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onToggle,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("${if (isMonitoring) "Stop" else "Start"} Monitoring")
        }
    }
}