import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import dev.stateholder.extensions.collectAsState

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
                Crossfade(state.status) { status ->
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
                    onClick = model::toggle,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("${if (state.active) "Stop" else "Start"} Monitoring")
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text)
    }
}