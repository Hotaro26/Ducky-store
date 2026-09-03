package com.hotaro.duckystore.theme

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("ducky_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0)) // 0: System, 1: Light, 2: Dark
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _appTheme = MutableStateFlow(
        AppTheme.values().firstOrNull { it.name == prefs.getString("app_theme", AppTheme.Dynamic.name) } ?: AppTheme.Dynamic
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    fun setThemeMode(mode: Int) {
        prefs.edit { putInt("theme_mode", mode) }
        _themeMode.value = mode
    }

    fun setAppTheme(theme: AppTheme) {
        prefs.edit { putString("app_theme", theme.name) }
        _appTheme.value = theme
    }
}
