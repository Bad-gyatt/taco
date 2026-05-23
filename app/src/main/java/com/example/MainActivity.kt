package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.theme.TacoTheme

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels { MainViewModelFactory(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeFlow by viewModel.theme.collectAsStateWithLifecycle()
      val isDark = when(themeFlow) {
          "dark", "coffee" -> true
          "light", "lavender" -> false
          else -> androidx.compose.foundation.isSystemInDarkTheme()
      }
      TacoTheme(darkTheme = isDark, themeName = themeFlow) {
        TacoApp(viewModel)
      }
    }
  }
}

