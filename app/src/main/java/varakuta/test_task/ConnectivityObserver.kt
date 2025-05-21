package varakuta.test_task

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    val isConnectionChanged: Flow<Boolean>
    val isConnected: Flow<Boolean>
}