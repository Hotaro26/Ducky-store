package com.hotaro.duckystore

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.hotaro.duckystore.ui.main.AppDetailScreen
import com.hotaro.duckystore.ui.main.MainScreen
import com.hotaro.duckystore.ui.main.SettingsScreen

@Composable
fun MainNavigation(themeManager: com.hotaro.duckystore.theme.ThemeManager) {
    val backStack = rememberNavBackStack(Main)
    val currentRoute = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            if (currentRoute == Main || currentRoute == Settings) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Main,
                        onClick = { if (currentRoute != Main) backStack.add(Main) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Settings,
                        onClick = { if (currentRoute != Settings) backStack.add(Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
            transitionSpec = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeIn() togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut()
            },
            popTransitionSpec = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeIn() togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut()
            },
            entryProvider = entryProvider {
                entry<Main> {
                    MainScreen(
                        onNavigateToDetail = { appDetail ->
                            backStack.add(appDetail)
                        },
                        modifier = Modifier
                    )
                }
                entry<Settings> {
                    SettingsScreen(themeManager = themeManager, modifier = Modifier)
                }
                entry<AppDetail> { navKey ->
                    AppDetailScreen(
                        detail = navKey,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier
                    )
                }
            }
        )
    }
}
