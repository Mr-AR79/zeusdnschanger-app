package ir.zeusdns.zeusdnschanger

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object VpnStatusManager {
    private val _vpnStatus = MutableSharedFlow<Boolean>(replay = 1)
    val vpnStatus: SharedFlow<Boolean> = _vpnStatus.asSharedFlow()

    suspend fun sendStatus(isConnected: Boolean) {
        _vpnStatus.emit(isConnected)
    }

    fun trySendStatus(isConnected: Boolean) {
        _vpnStatus.tryEmit(isConnected)
    }
}