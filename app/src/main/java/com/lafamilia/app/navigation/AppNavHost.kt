package com.lafamilia.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lafamilia.app.ui.auth.LoginScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) { LoginScreen(onLoginSuccess = { navController.navigate(Screen.ChatList.route) { popUpTo(Screen.Login.route) { inclusive = true } } }) }
        composable(Screen.Register.route) { PlaceholderScreen("Register") }
        composable(Screen.ChatList.route) { PlaceholderScreen("Chat list") }
        composable(Screen.Profile.route) { PlaceholderScreen("Profile") }
        composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            PlaceholderScreen("Chat $conversationId")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title)
    }
}