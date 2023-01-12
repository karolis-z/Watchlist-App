package com.myapplications.mywatchlist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.ui.details.DetailsScreen
import com.myapplications.mywatchlist.ui.search.SearchScreen
import com.myapplications.mywatchlist.ui.theme.MyWatchlistTheme
import com.myapplications.mywatchlist.ui.trending.TrendingScreen
import com.myapplications.mywatchlist.ui.watchlist.WatchlistScreen
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MAIN_ACTIVITY"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topLevelScreens = listOf(TopLevelScreens.Watchlist, TopLevelScreens.Search, TopLevelScreens.Trending)
        val allTopLevelScreens = listOf(TopLevelScreens.Watchlist, TopLevelScreens.Search, TopLevelScreens.Trending)

        setContent {

            val navController = rememberAnimatedNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentScreenTitleResId =
                allTopLevelScreens.find { it.route == currentDestination?.route }?.titleResId

            MyWatchlistTheme {

                val placeholderImage = if (isSystemInDarkTheme()) {
                    painterResource(id = R.drawable.placeholder_poster_dark)
                } else {
                    painterResource(id = R.drawable.placeholder_poster_light)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (currentScreenTitleResId == null) {
                                        "" // This works also for the Details Screen
                                    } else {
                                        stringResource(currentScreenTitleResId)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                if (currentDestination?.route?.contains(OtherScreens.Details.route) == true) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = stringResource(id = R.string.cd_back_arrow)
                                        )
                                    }
                                }
                            },
                        )
                    },
                    bottomBar = {
                        NavigationBar {
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
                    AnimatedNavHost(
                        navController = navController,
                        startDestination = TopLevelScreens.Watchlist.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(
                            route = TopLevelScreens.Watchlist.route
                        ) {
                            WatchlistScreen(
                                placeholderImage = placeholderImage,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route + "/${title.mediaId}&${title.type.name}")
                                })
                        }
                        composable(
                            route = TopLevelScreens.Search.route
                        ) {
                            SearchScreen(
                                placeholderImage = placeholderImage,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route + "/${title.mediaId}&${title.type.name}")
                                })
                        }
                        composable(
                            route = TopLevelScreens.Trending.route
                        ) {
                            TrendingScreen(
                                placeholderImage = placeholderImage,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route + "/${title.mediaId}&${title.type.name}")
                                })
                        }
                        composable(
                            route = OtherScreens.Details.route + "/{titleId}&{titleType}",
                            arguments = listOf(
                                navArgument("titleId") {type = NavType.LongType},
                                navArgument("titleType") {type = NavType.StringType}
                            ),
                            // TODO: Research and use more appropriate transitions
                            enterTransition = { fadeIn(animationSpec = tween(300)) },
                            exitTransition = { fadeOut(animationSpec = tween(300)) }
                        ) { backStackEntry ->
                            val titleId = backStackEntry.arguments?.getLong("titleId")
                            val titleType = backStackEntry.arguments?.getString("titleType")
                            if (titleId != null && titleType != null) {
                                DetailsScreen(titleId = titleId, titleType = titleType)
                            }
                        }
                    }
                }
            }
        }
    }
}

// TODO: Consider to add required icons as drawables to avoid the heavy extended icons library
sealed class TopLevelScreens(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    @StringRes val contentDescResId: Int
) {
    object Watchlist : TopLevelScreens(
        route = "watchlist",
        titleResId = R.string.title_watchlist,
        icon = Icons.Filled.Bookmarks,
        contentDescResId = R.string.cd_watchlist_icon
    )

    object Search : TopLevelScreens(
        route = "search",
        titleResId = R.string.title_search,
        icon = Icons.Filled.Search,
        contentDescResId = R.string.cd_search_icon
    )

    object Trending : TopLevelScreens(
        route = "trending",
        titleResId = R.string.title_trending,
        icon = Icons.Filled.TrendingUp,
        contentDescResId = R.string.cd_trending_icon
    )
}

sealed class OtherScreens(
    val route: String,
    @StringRes val titleResId: Int,
) {
    object Details : OtherScreens(
        route = "details",
        titleResId = R.string.title_details
    )
}