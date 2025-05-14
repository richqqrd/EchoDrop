package com.example.echodrop.view

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
                    navController.navigate("createPaket")
                },
                onSharePaket = { paketId ->
                    println("Navigating to paketDetail with ID: $paketId")
                    navController.navigate("paketDetail/${paketId.value}")
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

            PaketDetailScreen(
                paketId = paketId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}