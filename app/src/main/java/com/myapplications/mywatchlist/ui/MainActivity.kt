package com.myapplications.mywatchlist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.ui.components.SearchViewAction
import com.myapplications.mywatchlist.ui.search.SearchScreen
import com.myapplications.mywatchlist.ui.search.SearchViewModel
import com.myapplications.mywatchlist.ui.theme.MyWatchlistTheme
import com.myapplications.mywatchlist.ui.trending.TrendingScreen
import com.myapplications.mywatchlist.ui.watchlist.WatchlistScreen
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topLevelScreens = listOf(Screen.Watchlist, Screen.Search, Screen.Trending)
        val allScreens = listOf(Screen.Watchlist, Screen.Search, Screen.Trending)

        setContent {
            MyWatchlistTheme {

                val searchViewModel = hiltViewModel<SearchViewModel>()

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentScreenTitleResId =
                    allScreens.find { it.route == currentDestination?.route }?.titleResId

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (currentScreenTitleResId == null) {
                                        ""
                                    } else {
                                        stringResource(currentScreenTitleResId)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            actions = {
                                if (currentDestination?.route == Screen.Search.route) {
                                    SearchViewAction(
                                        placeholderText = stringResource(id = Screen.Search.titleResId),
                                        onSearchClicked = { searchViewModel.searchTitleClicked(it) }
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar() {
                            for (screen in topLevelScreens) {
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = stringResource(id = screen.contentDescResId)
                                        )
                                    },
                                    label = { Text(text = stringResource(id = screen.titleResId)) },
                                    selected = currentDestination?.hierarchy?.any {
                                        it.route == screen.route
                                    } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id)
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Watchlist.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Watchlist.route) {
                            WatchlistScreen()
                        }
                        composable(Screen.Search.route) {
                            SearchScreen(searchViewModel)
                        }
                        composable(Screen.Trending.route) {
                            TrendingScreen()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyWatchlistTheme {
        WatchlistScreen()
    }
}

// TODO: Consider to add required icons as drawables to avoid the heavy extended icons library
sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    @StringRes val contentDescResId: Int
) {
    object Watchlist : Screen(
        route = "watchlist",
        titleResId = R.string.title_watchlist,
        icon = Icons.Filled.Bookmarks,
        contentDescResId = R.string.cd_watchlist_icon
    )

    object Search : Screen(
        route = "search",
        titleResId = R.string.title_search,
        icon = Icons.Filled.Search,
        contentDescResId = R.string.cd_search_icon
    )

    object Trending : Screen(
        route = "trending",
        titleResId = R.string.title_trending,
        icon = Icons.Filled.TrendingUp,
        contentDescResId = R.string.cd_trending_icon
    )
}