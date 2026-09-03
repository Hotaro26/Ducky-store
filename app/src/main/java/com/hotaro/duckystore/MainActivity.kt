package com.hotaro.duckystore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hotaro.duckystore.theme.ExpressiveTheme
import com.hotaro.duckystore.theme.ThemeManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val themeManager = remember { ThemeManager(this) }
      val themeMode by themeManager.themeMode.collectAsState()
      val appTheme by themeManager.appTheme.collectAsState()
      
      val isDark = when (themeMode) {
          1 -> false
          2 -> true
          else -> isSystemInDarkTheme()
      }

      ExpressiveTheme(
          darkTheme = isDark,
          theme = appTheme
      ) { 
          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
              MainNavigation(themeManager) 
          } 
      }
    }
  }
}
