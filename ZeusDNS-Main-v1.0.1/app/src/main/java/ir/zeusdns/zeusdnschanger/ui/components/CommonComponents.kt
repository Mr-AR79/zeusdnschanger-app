package ir.zeusdns.zeusdnschanger.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.zeusdns.zeusdnschanger.data.NavItem
import ir.zeusdns.zeusdnschanger.data.ToastData
import ir.zeusdns.zeusdnschanger.data.ToastType
import ir.zeusdns.zeusdnschanger.ui.theme.CardBackgroundVariant
import ir.zeusdns.zeusdnschanger.ui.theme.GoldColor
import ir.zeusdns.zeusdnschanger.utils.Localization
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun UniversalToast(
    toastData: ToastData?,
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    if (toastData != null) {
        LaunchedEffect(toastData) {
            delay(2000)
            onDismiss()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (toastData.type) {
                        ToastType.SUCCESS -> Color(0xFF4CAF50)
                        ToastType.ERROR -> Color(0xFFF44336)
                        ToastType.INFO -> Color(0xFF2196F3)
                        ToastType.WARNING -> Color(0xFFFF9800)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (toastData.type) {
                            ToastType.SUCCESS -> Icons.Default.Check
                            ToastType.ERROR -> Icons.Default.Error
                            ToastType.INFO -> Icons.Default.Info
                            ToastType.WARNING -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = toastData.message,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    viewModel: MainViewModel
) {
    val isDarkTheme = viewModel.isDarkThemeEnabled()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        color = if (isDarkTheme) CardBackgroundVariant else Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .drawWithCache {
                        val strokeWidth = 1.5.dp.toPx()
                        onDrawWithContent {
                            drawContent()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GoldColor.copy(alpha = 0.2f),
                                        GoldColor.copy(alpha = 0.05f),
                                        GoldColor.copy(alpha = 0.2f)
                                    )
                                ),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                size = size.copy(
                                    width = size.width - strokeWidth,
                                    height = size.height - strokeWidth
                                ),
                                style = Stroke(width = strokeWidth),
                                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                            )
                        }
                    }
                    .shadow(
                        elevation = if (isDarkTheme) 25.dp else 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = GoldColor.copy(alpha = if (isDarkTheme) 0.4f else 0.2f),
                        ambientColor = GoldColor.copy(alpha = if (isDarkTheme) 0.2f else 0.1f),
                        clip = false
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = listOf(
                    NavItem(0, Icons.Default.HomeMax, "home"),
                    NavItem(1, Icons.Default.Dns, "dns"),
                    NavItem(2, Icons.Default.Verified, "pro"),
                    NavItem(3, Icons.Default.Settings, "settings")
                )

                items.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(item.id) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val isSelected = selectedItem == item.id

                        val indicatorAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = tween(durationMillis = 300),
                            label = "indicator"
                        )

                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .height(4.dp)
                                .width(28.dp)
                                .background(
                                    color = GoldColor.copy(
                                        alpha = indicatorAlpha * (if (isDarkTheme) 1f else 0.9f)
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                                .shadow(
                                    elevation = if (isSelected) (if (isDarkTheme) 8.dp else 4.dp) else 0.dp,
                                    shape = RoundedCornerShape(50),
                                    spotColor = GoldColor.copy(alpha = indicatorAlpha * 0.4f)
                                )
                        )

                        Icon(
                            imageVector = item.icon,
                            contentDescription = Localization.getString(item.key, viewModel),
                            tint = if (isSelected)
                                GoldColor
                            else
                                GoldColor.copy(alpha = if (isDarkTheme) 0.7f else 0.5f),
                            modifier = Modifier
                                .size(36.dp)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}