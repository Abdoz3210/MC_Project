package com.lafamilia.app.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ChatList : Screen("chat_list")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")

    data object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
}