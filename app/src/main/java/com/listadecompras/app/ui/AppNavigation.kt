package com.listadecompras.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.listadecompras.app.data.ShoppingRepository
import com.listadecompras.app.ui.detail.ListDetailScreen
import com.listadecompras.app.ui.detail.ListDetailViewModel
import com.listadecompras.app.ui.lists.ListsScreen
import com.listadecompras.app.ui.lists.ListsViewModel

private const val ROUTE_LISTS = "lists"
private const val ROUTE_DETAIL = "list/{listId}"

@Composable
fun AppNavigation(repository: ShoppingRepository) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = ROUTE_LISTS) {

        composable(ROUTE_LISTS) {
            val vm: ListsViewModel = viewModel(
                factory = viewModelFactory { initializer { ListsViewModel(repository) } }
            )
            ListsScreen(
                viewModel = vm,
                onOpenList = { id -> nav.navigate("list/$id") },
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("listId") { type = NavType.LongType }),
        ) { entry ->
            val listId = entry.arguments?.getLong("listId") ?: 0L
            val vm: ListDetailViewModel = viewModel(
                key = "detail-$listId",
                factory = viewModelFactory { initializer { ListDetailViewModel(repository, listId) } },
            )
            ListDetailScreen(
                viewModel = vm,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
