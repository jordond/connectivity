import co.touchlab.kermit.Logger
import dev.jordond.connectivity.Connectivity

actual fun platformConnectivity(): Connectivity {
    return Connectivity {
        pollingIntervalMs = 5000
        onPollResult { response ->
            Logger.d { "Poll Result: $response" }
        }
    }
}