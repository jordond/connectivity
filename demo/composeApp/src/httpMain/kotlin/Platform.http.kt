import co.touchlab.kermit.Logger
import dev.jordond.connectivity.Connectivity
import io.ktor.client.statement.request

actual fun platformConnectivity(): Connectivity {
    return Connectivity {
        pollingIntervalMs = 1000
        onHttpResponse { response ->
            Logger.d { "Connectivity: ${response.request}" }
        }
    }
}