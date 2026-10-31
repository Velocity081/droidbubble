package com.droidbubble.app

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.droidbubble.app.data.SettingsRepository
import com.droidbubble.app.service.IslandOverlayService
import com.droidbubble.app.theme.DroidbubbleTheme
import com.droidbubble.app.ui.main.MainScreen
import com.droidbubble.app.ui.main.MainScreenViewModel
import com.droidbubble.app.ui.main.PermissionHelper
import android.content.Intent
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainScreenViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainScreenViewModel(SettingsRepository(applicationContext)) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.checkPermissions(this)
        if (PermissionHelper.isOverlayGranted(this)) {
            IslandOverlayService.start(this)
        }
        if (intent.getBooleanExtra("EXTRA_SHOW_UPDATE", false)) {
            viewModel.checkForUpdates(this)
        }

        setContent {
            val settings by viewModel.settings.collectAsState()
            val fontFamily = remember(settings.fontKey) {
                com.droidbubble.app.theme.getIslandFontFamily(settings.fontKey)
            }
            DroidbubbleTheme(fontFamily = fontFamily) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("EXTRA_SHOW_UPDATE", false)) {
            viewModel.checkForUpdates(this)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions(this)
        if (PermissionHelper.isOverlayGranted(this)) {
            IslandOverlayService.start(this)
        }
    }
}
