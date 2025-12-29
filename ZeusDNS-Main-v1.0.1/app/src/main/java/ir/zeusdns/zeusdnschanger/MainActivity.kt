package ir.zeusdns.zeusdnschanger

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zeusdns.zeusdnschanger.ui.MainScreen
import ir.zeusdns.zeusdnschanger.ui.theme.GoldTheme
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
       // WindowCompat.enableEdgeToEdge(window)
       // WindowCompat.setDecorFitsSystemWindows(window, false)
       // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
       //     window.statusBarColor = android.graphics.Color.TRANSPARENT
       //     window.navigationBarColor = android.graphics.Color.TRANSPARENT
       // }
       // window.isStatusBarContrastEnforced = false
       // window.isNavigationBarContrastEnforced = false
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen(
                    onSplashFinished = { showSplash = false }
                )
            } else {
                val viewModel: MainViewModel = viewModel()
                GoldTheme(viewModel = viewModel) {
                    androidx.compose.material3.Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(viewModel)
                    }
                }
            }
        }
        requestCurrentVpnStatus()
    }

    private fun requestCurrentVpnStatus() {
        val intent = Intent(this, MyVpnService::class.java).apply {
            action = MyVpnService.ACTION_REQUEST_STATUS
        }
        startService(intent)
    }
}