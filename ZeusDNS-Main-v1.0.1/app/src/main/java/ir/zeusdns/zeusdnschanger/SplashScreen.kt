package ir.zeusdns.zeusdnschanger

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zeusdns.zeusdnschanger.ui.theme.DarkBackground
import ir.zeusdns.zeusdnschanger.ui.theme.GoldColor
import ir.zeusdns.zeusdnschanger.ui.theme.GoldTheme
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()

    var scale by remember { mutableFloatStateOf(0.8f) }
    var opacity by remember { mutableFloatStateOf(0f) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var loadingText by remember { mutableStateOf("Initializing...") }

    val animatedProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        delay(100)
        scale = 1f
        opacity = 1f
        loadingProgress = 0.3f
        loadingText = "Loading settings..."

        delay(400)
        loadingProgress = 0.6f
        loadingText = "Loading DNS servers..."

        delay(300)
        loadingProgress = 0.9f
        loadingText = "Checking DNS status..."

        delay(200)
        loadingProgress = 1f
        loadingText = "Ready!"
        delay(500)

        scale = 1.1f
        opacity = 0f
        delay(300)

        onSplashFinished()
    }

    GoldTheme(viewModel = viewModel) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldColor.copy(alpha = 0.3f),
                                DarkBackground,
                                DarkBackground
                            ),
                            center = center,
                            radius = size.maxDimension * 0.8f
                        ),
                        center = center,
                        radius = size.maxDimension * 0.8f
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulse by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_zeus_logo),
                        contentDescription = "ZeusDNS Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale * pulse)
                            .alpha(opacity),
                        colorFilter = ColorFilter.tint(GoldColor)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "ZeusDNS",
                        color = GoldColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .alpha(opacity)
                            .scale(scale)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fast & Safe",
                        color = GoldColor.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .alpha(opacity)
                            .scale(scale)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(80.dp),
                            color = GoldColor,
                            strokeWidth = 4.dp,
                            trackColor = GoldColor.copy(alpha = 0.2f)
                        )

                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = GoldColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = loadingText,
                        color = GoldColor.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.alpha(opacity)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = getAppVersion(context),
                        color = GoldColor.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

private fun getAppVersion(context: android.content.Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        "v${pInfo.versionName}"
    } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
        "v1.0.0"
    }
}
