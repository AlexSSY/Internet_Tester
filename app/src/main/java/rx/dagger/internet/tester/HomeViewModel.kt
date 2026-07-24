package rx.dagger.internet.tester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class HomeViewModel: ViewModel() {
    private val _speedStateFlow = MutableStateFlow(0f)
    val speed = _speedStateFlow.asStateFlow()

    private val _inProcess = MutableStateFlow(false)

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_inProcess.value) return@launch
            _inProcess.value = true
            _speedStateFlow.value = 0f

            val connection = URL(
                "http://212.183.159.230/1GB.zip"
            )
                .openConnection() as HttpURLConnection

            connection.connect()

            val buffer = ByteArray(64 * 1024)

            var bytesReceived = 0L
            var lastBytes = 0L
            var lastTime = System.nanoTime()

            connection.inputStream.use { input ->

                while (true) {

                    val read = input.read(buffer)
                    if (read == -1) break

                    bytesReceived += read

                    val now = System.nanoTime()
                    val elapsed = (now - lastTime) / 1_000_000_000f

                    if (elapsed >= 0.2f) {

                        val delta = bytesReceived - lastBytes

                        _speedStateFlow.value =
                            delta * 8f / elapsed / 1_000_000f

                        lastBytes = bytesReceived
                        lastTime = now
                    }
                }
            }

            connection.disconnect()

            _inProcess.value = false
        }
    }
}