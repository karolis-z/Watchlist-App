package com.myapplications.mywatchlist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.navigation.NavBackStackEntry
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
private const val TRANSITION_DURATION = 400
private val TRANSITION_EASING = FastOutSlowInEasing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val topLevelScreens =
        listOf(TopLevelScreens.Home, TopLevelScreens.Discover, TopLevelScreens.Watchlist)
    private val otherScreens = listOf(OtherScreens.TitleList, OtherScreens.Details)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val showTopAppBar = rememberSaveable { (mutableStateOf(false)) }
            val showUpButton = rememberSaveable { (mutableStateOf(false)) }

            WindowCompat.setDecorFitsSystemWindows(window, false)

            MyWatchlistTheme {

                val navController = rememberAnimatedNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val mTitleListType = remember { mutableStateOf<TitleListType?>(null) }

                // Determine which TopAppBar title to show
                /* TODO: Consider a better and more robust structure for the future. When new
                    screens are being introduced it will always require manual adjustment and / or
                    custom TopBar implementation in those new screens */
                val topBarTitle =
                    if (otherScreens.any { currentDestination?.route?.contains(it.route) == true }) {
                        /* If current destination is NOT a top level destination and previous
                        backstack was one of the top level screens, keeping its title. That's
                        because when transition animation is playing, we don't want to see the title
                        instantly change before it's off the screen. The next screen (Details or
                        Title List) have their own custom top app bars with their own titles */
                        val stringResId =
                            topLevelScreens.find {
                                it.route == navController.previousBackStackEntry?.destination?.route
                            }?.titleResId
                        if (stringResId == null) "" else stringResource(id = stringResId)
                    } else {
                        val stringResId =
                            topLevelScreens.find { it.route == currentDestination?.route }?.titleResId
                        if (stringResId == null) "" else stringResource(id = stringResId)
                    }

                if (currentDestination?.route?.contains(OtherScreens.Details.route) == true) {
                    showTopAppBar.value = false
                    showUpButton.value = false
                } else if (currentDestination?.route?.contains(OtherScreens.TitleList.route) == true) {
                    showTopAppBar.value = false
                    showUpButton.value = false
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
                        AnimatedVisibility(
                            visible = showTopAppBar.value,
                            enter =
                                fadeIn(tween(durationMillis = TRANSITION_DURATION, delayMillis = 0)),
                            exit =
                                fadeOut(tween(durationMillis = TRANSITION_DURATION, delayMillis = 0))
                        ) {
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
                            enterTransition = {
                                getEnterTransition(initialState, targetState, false)
                            },
                            exitTransition = {
                                getExitTransition(initialState, targetState, false)
                            },
                            popEnterTransition = {
                                getEnterTransition(initialState, targetState, true)
                            },
                            popExitTransition = {
                                getExitTransition(initialState, targetState, true)
                            }
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
                            enterTransition = {
                                getEnterTransition(initialState, targetState, false)
                            },
                            exitTransition = {
                                getExitTransition(initialState, targetState, false)
                            },
                            popEnterTransition = {
                                getEnterTransition(initialState, targetState, true)
                            },
                            popExitTransition = {
                                getExitTransition(initialState, targetState, true)
                            }
                        ) {
                            DiscoverScreen(
                                placeHolderPoster = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route +
                                            "/${title.mediaId}&${title.type.name}")
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                        composable(
                            route = TopLevelScreens.Watchlist.route,
                            enterTransition = {
                                getEnterTransition(initialState, targetState, false)
                            },
                            exitTransition = {
                                getExitTransition(initialState, targetState, false)
                            },
                            popEnterTransition = {
                                getEnterTransition(initialState, targetState, true)
                            },
                            popExitTransition = {
                                getExitTransition(initialState, targetState, true)
                            }
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
                            enterTransition = {
                                getEnterTransition(initialState, targetState, false)
                            },
                            exitTransition = {
                                getExitTransition(initialState, targetState, false)
                            },
                            popEnterTransition = {
                                getEnterTransition(initialState, targetState, true)
                            },
                            popExitTransition = {
                                getExitTransition(initialState, targetState, true)
                            }
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
                            enterTransition = {
                                getEnterTransition(initialState, targetState, false)
                            },
                            exitTransition = {
                                getExitTransition(initialState, targetState, false)
                            },
                            popEnterTransition = {
                                getEnterTransition(initialState, targetState, true)
                            },
                            popExitTransition = {
                                getExitTransition(initialState, targetState, true)
                            }
                        ) {
                            TitleListScreen(
                                placeholderPoster = placeholderPoster,
                                onTitleClicked = { title ->
                                    navController.navigate(route = OtherScreens.Details.route +
                                            "/${title.mediaId}&${title.type.name}")
                                },
                                onNavigateUp = { navController.navigateUp() },
                                contentPadding = paddingValues
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getEnterTransition(
        initialState: NavBackStackEntry,
        targetState: NavBackStackEntry,
        isPopTransition: Boolean,
    ): EnterTransition {

        /* Getting the initial and target screens by searching the top level screens and then other
       screens. Only OtherScreens have navigation arguments therefore checking with .contains */
        val initialScreen = topLevelScreens.find { initialState.destination.route == it.route }
            ?: otherScreens.find { initialState.destination.route?.contains(it.route) == true }
        val targetScreen = topLevelScreens.find { targetState.destination.route == it.route }
            ?: otherScreens.find { targetState.destination.route?.contains(it.route) == true }

        return when {
            // If initial and target are top level, fading in
            initialScreen is TopLevelScreens && targetScreen is TopLevelScreens ->
                fadeIn(tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING ))

            // If target is TopLevel, but initial is NOT, means we are going back, then slide in from left
            targetScreen is TopLevelScreens && initialScreen !is TopLevelScreens -> slideInHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                initialOffsetX = { width -> -width }
            )
            // If target is Details, and it's NOT a pop transition - entering by sliding in from right
            targetScreen is OtherScreens.Details && !isPopTransition -> slideInHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                initialOffsetX = { width -> width }
            )
            // If target is Details, and it IS a pop transition - entering from the left, because we're going back
            targetScreen is OtherScreens.Details && isPopTransition -> slideInHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                initialOffsetX = { width -> -width }
            )
            // If target is TitleList, but initial is Details, means we are going back, so slide in from left
            targetScreen is OtherScreens.TitleList && initialScreen is OtherScreens.Details -> slideInHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                initialOffsetX = { width -> -width }
            )
            // If target is TitleList, but initial is TopLevel, means we are going forward, then slide in from right
            targetScreen is OtherScreens.TitleList && initialScreen is TopLevelScreens -> slideInHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                initialOffsetX = { width -> width }
            )
            else -> EnterTransition.None
        }
    }

    private fun getExitTransition(
        initialState: NavBackStackEntry,
        targetState: NavBackStackEntry,
        isPopTransition: Boolean,
    ): ExitTransition {

        /* Getting the initial and target screens by searching the top level screens and then other
        screens. Only OtherScreens have navigation arguments therefore checking with .contains */
        val initialScreen = topLevelScreens.find { initialState.destination.route == it.route }
            ?: otherScreens.find { initialState.destination.route?.contains(it.route) == true }
        val targetScreen = topLevelScreens.find { targetState.destination.route == it.route }
            ?: otherScreens.find { targetState.destination.route?.contains(it.route) == true }

        return when {
            // If initial and target are top level, fading out
            initialScreen is TopLevelScreens && targetScreen is TopLevelScreens ->
                fadeOut(tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING))
            // If target is TopLevel, but initial is NOT, means we are going back, then slide out to right
            targetScreen is TopLevelScreens && initialScreen !is TopLevelScreens -> slideOutHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                targetOffsetX = { width -> width }
            )
            // If target is Details and it's NOT a pop transition - we're going forward, so slide out to left
            targetScreen is OtherScreens.Details && !isPopTransition -> slideOutHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                targetOffsetX = { width -> -width }
            )
            // If target is Details, and it IS pop transition - we're going back, so slide out to right
            targetScreen is OtherScreens.Details && isPopTransition -> slideOutHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                targetOffsetX = { width -> width }
            )
            // If target is TitleList, but initial is Details, means we are going back, then slide out to right
            targetScreen is OtherScreens.TitleList && initialScreen is OtherScreens.Details -> slideOutHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                targetOffsetX = { width -> width }
            )
            // If target is TitleList, but initial is TopLevel, means we are going forward, then slide out to left
            targetScreen is OtherScreens.TitleList && initialScreen is TopLevelScreens -> slideOutHorizontally(
                tween(durationMillis = TRANSITION_DURATION, easing = TRANSITION_EASING),
                targetOffsetX = { width -> -width }
            )
            else -> ExitTransition.None
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