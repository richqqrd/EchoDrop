package com.example.echodrop

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.echodrop.util.PermissionManager
import com.example.echodrop.view.EchoDropApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Alle Berechtigungen erteilt", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Einige Berechtigungen verweigert. WiFi Direct funktioniert möglicherweise nicht.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Überprüfe und fordere Berechtigungen an
        if (!permissionManager.arePermissionsGranted(this)) {
            requestPermissionLauncher.launch(permissionManager.getRequiredPermissions())
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EchoDropApp()
                }
            }
        }
    }
}