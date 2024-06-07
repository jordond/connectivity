package dev.jordond.connectivity

import kotlinx.coroutines.flow.Flow

public interface ConnectivityProvider {

    public fun monitor(): Flow<Connectivity.Status>
}

public fun ConnectivityProvider(
    flow: Flow<Connectivity.Status>,
): ConnectivityProvider = object : ConnectivityProvider {
    override fun monitor(): Flow<Connectivity.Status> = flow
}

public fun Flow<Connectivity.Status>.asProvider(): ConnectivityProvider = ConnectivityProvider(this)