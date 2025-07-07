package com.example.echodrop.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.echodrop.view.PaketDetailScreen

@Composable
fun EchoDropApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "inbox"
    ) {
        composable("inbox") {
            InboxScreen(
                onCreatePaket = {
                    navController.navigate("createPaket") {
                        // Configure navigation options
                        launchSingleTop = true
                    }
                },
                onSharePaket = { paketId ->
                    println("Navigating to paketDetail with ID: $paketId")
                    navController.navigate("paketDetail/${paketId.value}") {
                        // Configure navigation options
                        launchSingleTop = true
                    }
                },
                onOpenTransferManager = {
                    navController.navigate("transferManager") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("createPaket") {
            CreatePaketScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable("paketDetail/{paketId}") { backStackEntry ->
            val paketId = backStackEntry.arguments?.getString("paketId")
            println("Navigation erhalten: paketDetail mit ID: $paketId")

            if (paketId == null) {
                println("ERROR: paketId ist null!")
                return@composable
            }

            // Using PaketDetailScreen with PaketDetailViewModel
            PaketDetailScreen(
                paketId = paketId,
                onBackClick = { navController.popBackStack() },
                onOpenTransferManager = {
                    // Erst den PaketDetailScreen aus dem Backstack entfernen, dann zum TransferManager navigieren
                    navController.popBackStack()
                    navController.navigate("transferManager") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Neue Route für den TransferManagerScreen
        composable("transferManager") {
            TransferManagerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}