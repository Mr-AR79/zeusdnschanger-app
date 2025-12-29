package ir.zeusdns.zeusdnschanger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zeusdns.zeusdnschanger.ui.components.CustomBottomNavigationBar
import ir.zeusdns.zeusdnschanger.ui.screens.DnsContent
import ir.zeusdns.zeusdnschanger.ui.screens.HomeContent
import ir.zeusdns.zeusdnschanger.ui.screens.ProContent
import ir.zeusdns.zeusdnschanger.ui.screens.SettingsContent
import ir.zeusdns.zeusdnschanger.ui.theme.DarkBackground
import ir.zeusdns.zeusdnschanger.ui.theme.GoldColor
import ir.zeusdns.zeusdnschanger.ui.theme.LightBackground
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val isDarkTheme = viewModel.isDarkThemeEnabled()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ZeusDNS",
                        color = GoldColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isDarkTheme) DarkBackground else LightBackground
                )
            )
        },
        bottomBar = {
            CustomBottomNavigationBar(
                selectedItem = selectedItem,
                onItemSelected = { index -> selectedItem = index },
                viewModel = viewModel
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(if (isDarkTheme) DarkBackground else LightBackground)
        ) {
            when (selectedItem) {
                0 -> HomeContent(viewModel)
                1 -> DnsContent(viewModel)
                2 -> ProContent(viewModel)
                3 -> SettingsContent(viewModel)
            }
        }
    }
}