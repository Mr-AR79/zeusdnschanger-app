package ir.zeusdns.zeusdnschanger

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object IpUpdateStatusManager {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _lastKnownIp = MutableStateFlow<String?>(null)
    val lastKnownIp: StateFlow<String?> = _lastKnownIp.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<String?>(null)
    val lastExecutionResult: StateFlow<String?> = _lastExecutionResult.asStateFlow()

    fun setServiceStatus(isRunning: Boolean) {
        _isServiceRunning.value = isRunning
    }

    fun updateData(ip: String?, result: String?) {
        if (ip != null) _lastKnownIp.value = ip
        if (result != null) _lastExecutionResult.value = result
    }

    fun clearData() {
        _lastKnownIp.value = null
        _lastExecutionResult.value = null
    }
}