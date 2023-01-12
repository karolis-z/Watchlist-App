package com.myapplications.mywatchlist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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

            val showTopAppBar = rememberSaveable { (mutableStateOf(false)) }

            MyWatchlistTheme {

                val navController = rememberAnimatedNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentScreenTitleResId =
                    allTopLevelScreens.find { it.route == currentDestination?.route }?.titleResId

                when(currentDestination?.route?.contains(OtherScreens.Details.route)){
                    true -> showTopAppBar.value = false
                    false -> showTopAppBar.value = true
                    else -> Unit
                }

                val placeholderPoster = if (isSystemInDarkTheme()) {
                    painterResource(id = R.drawable.placeholder_poster_dark)
                } else {
                    painterResource(id = R.drawable.placeholder_poster_light)
                }
                val placeHolderBackdrop = if (isSystemInDarkTheme()) {
                    painterResource(id = R.drawable.placeholder_backdrop_dark)
                } else {
                    painterResource(id = R.drawable.placeholder_backdrop_light)
                }
                val placeHolderPortrait = if (isSystemInDarkTheme()) {
                    painterResource(id = R.drawable.placeholder_portrait_dark)
                } else {
                    painterResource(id = R.drawable.placeholder_portrait_light)
                }

                Scaffold(
                    topBar = {
                        MyTopAppBar(
                            title = if (currentScreenTitleResId == null) {
                                "" // This works also for the Details Screen
                            } else {
                                stringResource(currentScreenTitleResId)
                            },
                            showLargeTopAppBar = showTopAppBar,
//                            showUpButton = showUpButton,
//                            onNavigateUp = { navController.navigateUp() })
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
                            route = TopLevelScreens.Watchlist.route,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            WatchlistScreen(
                                placeholderImage = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route + "/${title.mediaId}&${title.type.name}")
                                })
                        }
                        composable(
                            route = TopLevelScreens.Search.route,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            SearchScreen(
                                placeholderImage = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route + "/${title.mediaId}&${title.type.name}")
                                })
                        }
                        composable(
                            route = TopLevelScreens.Trending.route,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            TrendingScreen(
                                placeholderImage = placeholderPoster,
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
                            enterTransition = { fadeIn (animationSpec = tween(500)) },
                            exitTransition = { fadeOut (animationSpec = tween(500)) }
                        ) { backStackEntry ->
                            val titleId = backStackEntry.arguments?.getLong("titleId")
                            val titleType = backStackEntry.arguments?.getString("titleType")
                            if (titleId != null && titleType != null) {
                                DetailsScreen(
                                    titleId = titleId,
                                    titleType = titleType,
                                    placeHolderPortrait = placeHolderPortrait,
                                    placeHolderBackdrop = placeHolderBackdrop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(
    title: String,
    showLargeTopAppBar: MutableState<Boolean>,
//    showUpButton: MutableState<Boolean>,
//    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
){
    // TODO: TESTING ANOTHER OPTION
    // Keeping it like this for a while to test a custom toolbar layout
    if (showLargeTopAppBar.value) {
        TopAppBar(
            title = {
                Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            modifier = modifier
        )
    } else {
        return
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