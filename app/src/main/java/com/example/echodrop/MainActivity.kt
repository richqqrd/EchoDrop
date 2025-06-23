package com.example.echodrop

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.echodrop.util.PermissionManager
import com.example.echodrop.view.EchoDropApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    // Status für den Berechtigungsdialog
    private var showPermissionDialog = false
    private var permissionsToRequest: List<String> = emptyList()
    
    // Erstelle einen ActivityResultLauncher für Berechtigungsanfragen
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Alle Berechtigungen wurden erteilt
            Toast.makeText(
                this,
                "Alle Berechtigungen erteilt!",
                Toast.LENGTH_SHORT
            ).show()
            
            // App neu starten ohne Dialog
            showPermissionDialog = false
            recreateContent()
        } else {
            // Einige Berechtigungen wurden verweigert
            val missing = permissions.entries.filter { !it.value }.map { it.key }
            // Erkläre dem Benutzer, warum die Berechtigungen wichtig sind
            permissionsToRequest = missing
            showPermissionDialog = true
            recreateContent()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prüfe Berechtigungen beim Start
        checkAndRequestPermissions()
    }
    
    private fun recreateContent() {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showPermissionDialog) {
                        PermissionDialog(
                            permissions = permissionsToRequest,
                            onConfirm = {
                                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                            },
                            onDismiss = {
                                // Bei Ablehnung trotzdem die App anzeigen, aber mit eingeschränkter Funktionalität
                                Toast.makeText(
                                    this,
                                    "Einige Funktionen sind ohne die benötigten Berechtigungen nicht verfügbar.",
                                    Toast.LENGTH_LONG
                                ).show()
                                showPermissionDialog = false
                                recreateContent()
                            }
                        )
                    } else {
                        EchoDropApp()
                    }
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (!permissionManager.arePermissionsGranted(this)) {
            permissionsToRequest = permissionManager.getMissingPermissions(this)
            if (permissionsToRequest.isNotEmpty()) {
                if (shouldShowRequestPermissionRationale(permissionsToRequest.first())) {
                    showPermissionDialog = true
                    recreateContent()
                } else {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            } else {
                recreateContent()
            }
        } else {
            recreateContent()
        }
    }
}

@Composable
fun PermissionDialog(
    permissions: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = buildString {
        append("EchoDrop benötigt folgende Berechtigungen, um korrekt zu funktionieren:\n\n")
        
        if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            append("• Standort: Um WiFi Direct Geräte in der Nähe zu finden\n")
        }
        
        if (permissions.contains(Manifest.permission.NEARBY_WIFI_DEVICES)) {
            append("• WLAN-Geräte in der Nähe: Für WiFi Direct Verbindungen\n")
        }
        
        if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) || 
            permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            append("• Speicher: Zum Speichern empfangener Dateien\n")
        }
        
        append("\nMöchtest du diese Berechtigungen jetzt erteilen?")
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Berechtigungen benötigt") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ja")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nein")
            }
        }
    )
}