package com.example.chatapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatapp.presentation.chat.ChatScreen
import com.example.chatapp.presentation.login.LoginScreen
import com.example.chatapp.presentation.register.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("chat") {
            ChatScreen(navController)
        }
    }
}
