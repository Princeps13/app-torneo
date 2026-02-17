package com.lucas.app_torneo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lucas.app_torneo.ui.screens.CreateTournamentScreen
import com.lucas.app_torneo.ui.screens.HomeScreen
import com.lucas.app_torneo.ui.screens.TournamentDetailScreen
import com.lucas.app_torneo.ui.viewmodel.AppContainer
import com.lucas.app_torneo.ui.viewmodel.AppViewModelFactory
import com.lucas.app_torneo.ui.viewmodel.CreateTournamentViewModel
import com.lucas.app_torneo.ui.viewmodel.HomeViewModel
import com.lucas.app_torneo.ui.viewmodel.TournamentDetailViewModel

@Composable
fun TorneoNavApp() {
    val nav = rememberNavController()
    val container = AppContainer(LocalContext.current)

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = AppViewModelFactory(container))
            HomeScreen(vm, onCreate = { nav.navigate("create") }, onOpenTournament = { nav.navigate("detail/$it") })
        }
        composable("create") {
            val vm: CreateTournamentViewModel = viewModel(factory = AppViewModelFactory(container))
            CreateTournamentScreen(vm) { id ->
                nav.navigate("detail/$id") { popUpTo("home") }
            }
        }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val vm: TournamentDetailViewModel = viewModel(factory = AppViewModelFactory(container, tournamentId = id))
            TournamentDetailScreen(vm)
        }
    }
}
