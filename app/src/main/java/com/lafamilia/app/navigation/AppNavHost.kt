package com.lafamilia.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lafamilia.app.AppGraph
import com.lafamilia.app.ui.auth.LoginScreen
import com.lafamilia.app.ui.auth.RegisterScreen
import com.lafamilia.app.ui.chat.ChatScreen
import com.lafamilia.app.ui.chatlist.ChatListScreen
import com.lafamilia.app.ui.profile.ProfileScreen
import com.lafamilia.app.ui.settings.SettingsScreen

/**
 * The map of the app: which route shows which screen.
 * The NavHost decides WHAT to show; the NavController moves between screens.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {

    // Session handling: a user who is already signed in skips the login screen.
    val startDestination = if (AppGraph.authRepository.isSignedIn) {
        Screen.ChatList.route
    } else {
        Screen.Login.route
    }

    val goToChatListAndForgetAuth: () -> Unit = {
        navController.navigate(Screen.ChatList.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = goToChatListAndForgetAuth,
                onGoToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegistered = goToChatListAndForgetAuth,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                onOpenChat = { conversationId -> navController.navigate(Screen.Chat.createRoute(conversationId)) },
                onOpenProfile = { navController.navigate(Screen.Profile.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ChatList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
