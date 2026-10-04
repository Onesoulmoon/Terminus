package com.necroware.terminusplayer

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.ui.components.TerminalMatrixBackground
import com.necroware.terminusplayer.ui.navigation.TerminusNavGraph
import com.necroware.terminusplayer.ui.theme.TerminusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingExternalUriState = mutableStateOf<android.net.Uri?>(null)

    private val essentialPermissions: Array<String>
        get() {
            val list = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            return list.toTypedArray()
        }

    private fun allPermissionsGranted() = essentialPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun mediaUriFromIntent(intent: Intent?): android.net.Uri? =
        intent?.takeIf { it.action == Intent.ACTION_VIEW }?.data

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingExternalUriState.value = mediaUriFromIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the player full screen by removing the status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContent {
            val activityViewModel: MainActivityViewModel = hiltViewModel()
            val currentTheme by activityViewModel.currentTheme.collectAsStateWithLifecycle()

            var handledExternalUri by remember { mutableStateOf<android.net.Uri?>(null) }
            val pendingExternalUri by pendingExternalUriState

            TerminusTheme(preset = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasEssentialPermission by remember {
                        mutableStateOf(allPermissionsGranted())
                    }

                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { result -> 
                        hasEssentialPermission = result.values.all { it } || allPermissionsGranted()
                    }

                    if (hasEssentialPermission) {
                        val incomingUri = pendingExternalUri ?: mediaUriFromIntent(intent)
                        val userPrefs by activityViewModel.userPreferences.collectAsStateWithLifecycle()

                        LaunchedEffect(incomingUri) {
                            if (incomingUri != null && incomingUri != handledExternalUri) {
                                handledExternalUri = incomingUri
                                activityViewModel.playExternalUri(incomingUri)
                                pendingExternalUriState.value = null
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (userPrefs.matrixBgEnabled) {
                                TerminalMatrixBackground(
                                    accentColor = currentTheme.accent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            TerminusNavGraph()
                        }
                    } else {
                        PermissionGate(onRequestPermission = { permissionLauncher.launch(essentialPermissions) })
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PermissionGate(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "> TERMINUS_",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "[ audio library access required ]",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onRequestPermission) {
            Text("GRANT ACCESS")
        }
    }
}
