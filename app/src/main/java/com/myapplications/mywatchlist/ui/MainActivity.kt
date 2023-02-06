package com.myapplications.mywatchlist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.view.WindowCompat
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
import com.myapplications.mywatchlist.ui.discover.DiscoverScreen
import com.myapplications.mywatchlist.ui.entities.TitleListType
import com.myapplications.mywatchlist.ui.home.HomeScreen
import com.myapplications.mywatchlist.ui.theme.MyWatchlistTheme
import com.myapplications.mywatchlist.ui.titlelist.TitleListScreen
import com.myapplications.mywatchlist.ui.watchlist.WatchlistScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "MAIN_ACTIVITY"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topLevelScreens = listOf(TopLevelScreens.Home, TopLevelScreens.Discover, TopLevelScreens.Watchlist)

        setContent {

            val showTopAppBar = rememberSaveable { (mutableStateOf(false)) }
            val showUpButton = rememberSaveable { (mutableStateOf(false)) }

            WindowCompat.setDecorFitsSystemWindows(window, false)

            MyWatchlistTheme {

                val navController = rememberAnimatedNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val mTitleListType = remember { mutableStateOf<TitleListType?>(null) }

                // Determined which TopAppBar title to show
                val topBarTitle =
                    if (currentDestination?.route?.contains(OtherScreens.Details.route) == true) {
                        ""
                    } else if (currentDestination?.route?.contains(OtherScreens.TitleList.route) == true) {
                        when (mTitleListType.value) {
                            TitleListType.Trending ->
                                stringResource(id = R.string.titlelist_screen_title_trending)
                            TitleListType.Popular ->
                                stringResource(id = R.string.titlelist_screen_title_popular)
                            TitleListType.TopRated ->
                                stringResource(id = R.string.titlelist_screen_title_toprated)
                            TitleListType.UpcomingMovies ->
                                stringResource(id = R.string.titlelist_screen_title_upcoming_movies)
                            null -> ""
                        }
                    } else {
                        val stringResId =
                            topLevelScreens.find { it.route == currentDestination?.route }?.titleResId
                        if (stringResId == null) "" else stringResource(id = stringResId)
                    }

                if (currentDestination?.route?.contains(OtherScreens.Details.route) == true) {
                    showTopAppBar.value = false
                    showUpButton.value = false
                } else if (currentDestination?.route?.contains(OtherScreens.TitleList.route) == true) {
                    showTopAppBar.value = true
                    showUpButton.value = true
                } else {
                    showTopAppBar.value = true
                    showUpButton.value = false
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

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        if (showTopAppBar.value) {
                            MyTopAppBar(
                                title = topBarTitle,
                                showUpButton = showUpButton.value,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(snackbarHostState)
                    },
                    bottomBar = {
                        NavigationBar {
                            for (screen in topLevelScreens) {
                                NavigationBarItem(
                                    icon = {
                                        Crossfade(targetState = currentDestination) {
                                            when (it?.route) {
                                                screen.route -> Icon(
                                                    imageVector = screen.selectedIcon,
                                                    contentDescription =
                                                        stringResource(id = screen.contentDescResId)
                                                )
                                                else -> Icon(
                                                    imageVector = screen.unselectedIcon,
                                                    contentDescription =
                                                        stringResource(id = screen.contentDescResId)
                                                )
                                            }
                                        }
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
                        startDestination = TopLevelScreens.Home.route,
                        /* NOT adding the Scaffold provided padding values here because we want to
                        * individually adjust for insets in each screen, because in Details screen
                        * we show the backdrop image under the status bar. Appropriate padding
                        * values must be passed on to each screen and used however needed. */
                        modifier = Modifier
                    ) {
                        composable(
                            route = TopLevelScreens.Home.route,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            HomeScreen(
                                placeholderPoster = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(
                                        route = OtherScreens.Details.route +
                                                "/${title.mediaId}&${title.type.name}"
                                    )
                                },
                                onSeeAllClicked = { titleListType ->
                                    mTitleListType.value = titleListType
                                    navController.navigate(route =
                                        OtherScreens.TitleList.route + "/${titleListType.name}"
                                    )
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                        composable(
                            route = TopLevelScreens.Discover.route,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            DiscoverScreen(
                                placeholderImage = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route +
                                            "/${title.mediaId}&${title.type.name}")
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                        composable(
                            route = TopLevelScreens.Watchlist.route,
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            WatchlistScreen(
                                placeholderImage = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route +
                                            "/${title.mediaId}&${title.type.name}")
                                },
                                onShowSnackbar = {
                                    scope.launch { snackbarHostState.showSnackbar(it) }
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                        composable(
                            route = OtherScreens.Details.route +
                                    "/{${NavigationArgument.MEDIA_ID.value}}&{${NavigationArgument.TITLE_TYPE.value}}",
                            arguments = listOf(
                                navArgument(NavigationArgument.MEDIA_ID.value) {
                                    type = NavType.LongType
                                },
                                navArgument(NavigationArgument.TITLE_TYPE.value) {
                                    type = NavType.StringType
                                }
                            ),
                            // TODO: Research and use more appropriate transitions
                            enterTransition = { fadeIn (animationSpec = tween(500)) },
                            exitTransition = { fadeOut (animationSpec = tween(500)) }
                        ) {
                            DetailsScreen(
                                placeHolderBackdrop = placeHolderBackdrop,
                                placeHolderPortrait = placeHolderPortrait,
                                placeholderPoster = placeholderPoster,
                                onNavigateUp = { navController.navigateUp() },
                                onSimilarOrRecommendedTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route +
                                            "/${title.mediaId}&${title.type.name}")
                                },
                                modifier = Modifier.padding(
                                    bottom = paddingValues.calculateBottomPadding()
                                )
                            )
                        }
                        composable(
                            route = OtherScreens.TitleList.route
                                    + "/{${NavigationArgument.TITLE_LIST_TYPE.value}}",
                            arguments = listOf(
                                navArgument(NavigationArgument.TITLE_LIST_TYPE.value) {
                                    type = NavType.StringType
                                }
                            ),
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
                            TitleListScreen(
                                placeholderPoster = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route +
                                            "/${title.mediaId}&${title.type.name}")
                                },
                                contentPadding = paddingValues
                            )
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
    showUpButton: Boolean,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (showUpButton) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.cd_back_arrow)
                    )
                }
            }
        },
        modifier = modifier
    )
}

// TODO: Consider to add required icons as drawables to avoid the heavy extended icons library
sealed class TopLevelScreens(
    val route: String,
    @StringRes val titleResId: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    @StringRes val contentDescResId: Int
) {
    object Home : TopLevelScreens(
        route = "home",
        titleResId = R.string.title_home,
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        contentDescResId = R.string.cd_home_icon
    )

    object Discover : TopLevelScreens(
        route = "discover",
        titleResId = R.string.title_discover,
        unselectedIcon = Icons.Filled.Search,
        selectedIcon = Icons.Filled.Search,
        contentDescResId = R.string.cd_discover_icon
    )

    object Watchlist : TopLevelScreens(
        route = "watchlist",
        titleResId = R.string.title_watchlist,
        unselectedIcon = Icons.Outlined.Bookmarks,
        selectedIcon = Icons.Filled.Bookmarks,
        contentDescResId = R.string.cd_watchlist_icon
    )
}

sealed class OtherScreens(val route: String) {
    object Details : OtherScreens(route = "details")
    object TitleList : OtherScreens(route = "title_list")
}

enum class NavigationArgument(val value: String) {
    MEDIA_ID("mediaId"),
    TITLE_TYPE("titleType"),
    TITLE_LIST_TYPE("titleListType")
}