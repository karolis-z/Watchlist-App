package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.util.DateFormatter
import com.myapplications.mywatchlist.domain.entities.*
import com.myapplications.mywatchlist.ui.components.AnimatedWatchlistButton
import com.myapplications.mywatchlist.ui.components.ErrorText
import com.myapplications.mywatchlist.ui.components.GenreChip
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.details.toolbarstate.ExitUntilCollapsedState
import com.myapplications.mywatchlist.ui.details.toolbarstate.ToolbarState
import com.myapplications.mywatchlist.ui.theme.IMDBOrange
import java.time.LocalDate
import kotlin.math.roundToInt

private const val MAX_SUMMARY_LINES = 5
private val StandardToolbarHeight = 64.dp
private val NavButtonGradientSize = 40.dp
// This considers top padding for a 48x48dp IconButton. This size is for accessibility
// start = end = ((standard total width (16pad + 24 icon + 16pad)=56) - 48)/2 = 4.dp
private val NavigationIconPadding =
    PaddingValues(
        start = 4.dp,
        top = (StandardToolbarHeight - 48.dp) / 2,
        end = 4.dp,
        bottom = (StandardToolbarHeight - 48.dp) / 2
    )
private val StandardHzPadding = 16.dp
private val ExpandedStateTitlePadding =
    PaddingValues(start = StandardHzPadding, top = 0.dp, end = 0.dp, bottom = 10.dp)
private const val TAG = "DETAILS_SCREEN"

@Composable
fun DetailsScreen(
    placeHolderBackdrop: Painter,
    placeHolderPortrait: Painter,
    placeholderPoster: Painter,
    onNavigateUp: () -> Unit,
    onSimilarOrRecommendedTitleClicked: (TitleItemMinimal) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculating status bar height
    val statusBarPadding =
        WindowInsets.statusBars.asPaddingValues(LocalDensity.current).calculateTopPadding()

    // Calculating the toolbar height, by the expected image's aspect ratio and the screen width
    val maxToolbarHeight =
        (LocalConfiguration.current.screenWidthDp / Constants.BACKDROP_IMAGE_ASPECT_RATIO).dp
    val minToolbarHeight = StandardToolbarHeight + statusBarPadding


    val toolbarHeightRange = with(LocalDensity.current) {
        minToolbarHeight.roundToPx()..maxToolbarHeight.roundToPx()
    }
    val scrollState = rememberScrollState()
    val toolbarState = rememberToolbarState(toolbarHeightRange = toolbarHeightRange)
    toolbarState.scrollValue = scrollState.value

    val viewModel = hiltViewModel<DetailsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    Log.d(TAG, "DetailsScreen: uiState = $uiState")

    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme,
        )
    }

    when (uiState) {
        DetailsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingCircle()
            }
        }
        is DetailsUiState.Error -> {
            Box(modifier = modifier.fillMaxSize()) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.padding(
                        top = statusBarPadding + NavigationIconPadding.calculateTopPadding(),
                        start = NavigationIconPadding.calculateStartPadding(LocalLayoutDirection.current)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.cd_back_arrow),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = StandardHzPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val errorMessage = when ((uiState as DetailsUiState.Error).error) {
                        DetailsError.NoInternet -> stringResource(id = R.string.error_no_internet_connection)
                        DetailsError.FailedApiRequest -> stringResource(id = R.string.details_error_failed_api_request)
                        DetailsError.Unknown -> stringResource(id = R.string.error_something_went_wrong)
                    }
                    ErrorText(
                        errorMessage = errorMessage,
                        onButtonRetryClick = { viewModel.initializeData() })
                }
            }
        }
        is DetailsUiState.Ready -> {
            val title = (uiState as DetailsUiState.Ready).title
            val type = (uiState as DetailsUiState.Ready).type

            // Setting to data unavailable string, but will be replaced below if actually available
            var runtimeOrSeasonsString = stringResource(id = R.string.details_data_notavailable)
            when (type) {
                TitleType.TV -> {
                    runtimeOrSeasonsString = pluralStringResource(
                        id = R.plurals.details_seasons,
                        count = (title as TV).numberOfSeasons,
                        title.numberOfSeasons
                    )
                }
                TitleType.MOVIE -> {
                    val runtime = (title as Movie).runtime
                    if (runtime != null) {
                        val hoursAndMinutesPair =
                            viewModel.convertRuntimeToHourAndMinutesPair(runtime)
                        runtimeOrSeasonsString = stringResource(
                            id = R.string.details_runtime,
                            hoursAndMinutesPair.first,
                            hoursAndMinutesPair.second
                        )
                    }
                }
            }
            Box(modifier = modifier) {
                DetailsScreenContent(
                    title = title,
                    titleType = type,
                    videos = (uiState as DetailsUiState.Ready).videos,
                    player = viewModel.player,
                    onVideoSelected = { viewModel.onVideoSelected(it) } ,
                    runtimeOrSeasonsString = runtimeOrSeasonsString,
                    placeHolderPortrait = placeHolderPortrait,
                    placeHolderBackdrop = placeHolderBackdrop,
                    placeholderPoster = placeholderPoster,
                    onWatchlistClicked = { viewModel.onWatchlistClicked() },
                    onSimilarOrRecommendedTitleClicked = onSimilarOrRecommendedTitleClicked,
                    playerState = playerState,
                    scrollState = scrollState,
                    contentPadding = PaddingValues(top = maxToolbarHeight),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = StandardHzPadding)
                )
                DetailsCollapsingToolbar(
                    progress = toolbarState.progress,
                    onNavigateUp = onNavigateUp,
                    titleName = title.name,
                    backdropLink = title.backdropLink,
                    placeHolderBackdrop = placeHolderBackdrop,
                    maxToolbarHeight = maxToolbarHeight,
                    statusBarPadding = statusBarPadding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(LocalDensity.current) { toolbarState.height.toDp() })
                        .graphicsLayer { translationY = toolbarState.offset }
                )
            }
        }
    }
}

@Composable
private fun rememberToolbarState(toolbarHeightRange: IntRange): ToolbarState {
    return rememberSaveable(saver = ExitUntilCollapsedState.saver) {
        ExitUntilCollapsedState(toolbarHeightRange)
    }
}

@Composable
fun DetailsCollapsingToolbar(
    progress: Float,
    onNavigateUp: () -> Unit,
    titleName: String,
    backdropLink: String?,
    placeHolderBackdrop: Painter,
    maxToolbarHeight: Dp,
    statusBarPadding: Dp,
    modifier: Modifier = Modifier
) {

    val expandedToolbarHeight = with(LocalDensity.current) { maxToolbarHeight.roundToPx() }

    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = if (progress == 0f) 3.dp else 0.dp,
        modifier = modifier
    ) {
        Box (modifier = Modifier.fillMaxSize()) {
            //#region Background Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backdropLink)
                    .crossfade(true)
                    .build(),
                placeholder = placeHolderBackdrop,
                fallback = placeHolderBackdrop,
                error = placeHolderBackdrop,
                contentDescription = null, //Decorative
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    // When fully collapsed, the image shall be invisible
                    .graphicsLayer { alpha = progress }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            ),
                            startY = expandedToolbarHeight.toFloat() / 3,
                        ),
                        alpha = (progress * 2).coerceAtMost(1f)
                    )
            )
            //#endregion
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                DetailsCollapsingToolbarLayout (
                    progress = progress,
                    statusBarPadding = statusBarPadding
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.layoutId(CollapsingToolbarContent.NavUpButton)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(NavButtonGradientSize)
                                .align(Alignment.Center)
                                .background(
                                    color = MaterialTheme.colorScheme.background.copy(
                                        alpha = progress * 0.64f
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(id = R.string.cd_back_arrow),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    Text(
                        text = titleName,
                        style =  MaterialTheme.typography.displaySmall.copy(
                            fontSize = lerp(start = 22f, stop = 36f, fraction = progress).sp
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = lerp(1, 2, progress),
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .layoutId(CollapsingToolbarContent.TitleText)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsCollapsingToolbarLayout(
    progress: Float,
    statusBarPadding: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val placeablesMap = mutableMapOf<CollapsingToolbarContent, Placeable>()
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        placeablesMap.clear()

        val contentItems = CollapsingToolbarContent.values().toList()
        check(measurables.size == contentItems.size)

        val direction = this.layoutDirection

        val navIconX = NavigationIconPadding.calculateStartPadding(direction).roundToPx()
        val navIconY =
            NavigationIconPadding.calculateTopPadding().roundToPx() + statusBarPadding.roundToPx()
        val centerOfCollapsedY =
            StandardToolbarHeight.roundToPx() / 2 + statusBarPadding.roundToPx()

        val expandedTitleStartOffset = ExpandedStateTitlePadding.calculateStartPadding(direction).roundToPx()
        val expandedTitleBottomOffset = ExpandedStateTitlePadding.calculateBottomPadding().roundToPx()

        val placeables = mutableListOf<Placeable>()

        measurables.forEach { measurable ->
            val placeable = when(measurable.layoutId as CollapsingToolbarContent) {
                CollapsingToolbarContent.NavUpButton -> {
                    measurable.measure(constraints)
                }
                CollapsingToolbarContent.TitleText -> {
                    val width = lerp(
                        start = constraints.maxWidth - placeables[0].width -
                                navIconX * 2 - StandardHzPadding.roundToPx(),
                        stop = constraints.maxWidth - StandardHzPadding.roundToPx() * 2,
                        fraction = progress
                    )
                    measurable.measure(Constraints.fixedWidth(width))
                }
            }
            placeablesMap[measurable.layoutId as CollapsingToolbarContent] = placeable
            placeables.add(placeable)
        }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {

            // Just in case check if placeables list is the same size as the placeablesMap
            check(placeables.size == placeablesMap.size)

            //Creating references to the content elements being placed in the layout
            var upIcon: Placeable? = null
            var title: Placeable? = null

            for (key in placeablesMap.keys) {
                when(key) {
                    CollapsingToolbarContent.NavUpButton -> upIcon = placeablesMap[key]
                    CollapsingToolbarContent.TitleText -> title = placeablesMap[key]
                }
            }
            if (upIcon == null || title == null) {
                throw IllegalStateException("Up Icon or Title were null but were not supposed to be")
            }

            /* Calculating Start (collapsed toolbar) and Stop (expanded toolbar) 'y' positions of
            title. NOT USING AT THIS POINT. But keeping in case there's a need later. */
            // val titleStartY = centerOfCollapsedY - title.height / 2
            // val titleStopY = constraints.maxHeight - title.height - expandedTitleBottomOffset

            // Placing the placeables
            upIcon.placeRelative(x = navIconX, y = navIconY)

            /* Using a quadratic Bezier curve for 'slower' change of Y coordinate so the title
            doesn't jump so quickly to the top when starting scrolling */
            val titleYfirstInterpolatedPoint = lerp(
                start = (constraints.maxHeight * 0.75f).roundToInt(),
                stop = constraints.maxHeight - title.height - expandedTitleBottomOffset,
                fraction = progress
            )
            val titleYsecondInterpolatedPoint= lerp(
                start = centerOfCollapsedY - title.height / 2,
                stop = (constraints.maxHeight * 0.75f).roundToInt(),
                fraction = progress
            )
            val titleY = lerp(
                start = titleYsecondInterpolatedPoint,
                stop = titleYfirstInterpolatedPoint,
                fraction = progress
            )

            title.placeRelative(
                x = lerp(
                    start = upIcon.width + navIconX * 2,    // start = collapsed toolbar
                    stop =  expandedTitleStartOffset,       // stop = expanded toolbar
                    fraction = progress
                ),
                y = titleY
                // Left the previous implementation in case of need to go back
                // y = lerp(
                //     start = titleStartY,  // start = collapsed toolbar
                //     stop = titleStopY,    // stop = expanded toolbar
                //     fraction = progress
                // )
            )
        }
    }
}

enum class CollapsingToolbarContent {
    NavUpButton,
    TitleText
}

@Composable
fun DetailsScreenContent(
    title: Title,
    titleType: TitleType,
    videos: List<YtVideoUiModel>?,
    player: Player,
    onVideoSelected: (YtVideoUiModel) -> Unit,
    playerState: Int,
    runtimeOrSeasonsString: String,
    placeHolderPortrait: Painter,
    placeHolderBackdrop: Painter,
    placeholderPoster: Painter,
    onWatchlistClicked: () -> Unit,
    onSimilarOrRecommendedTitleClicked: (TitleItemMinimal) -> Unit,
    scrollState: ScrollState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var expandedSummaryState by remember { mutableStateOf(false) }
    var showExpandSummaryArrow by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expandedSummaryState) 180f else 0f)

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentPadding.calculateTopPadding())
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            //#region MAIN DETAILS
            DetailsInfoRow(
                title = title,
                titleType = titleType,
                modifier = Modifier.fillMaxWidth(),
                runtimeOrSeasonsString = runtimeOrSeasonsString
            )
            Spacer(modifier = Modifier.height(15.dp))
            //#endregion

            //#region GENRES
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(key = { genre -> genre.id }, items = title.genres) { genre: Genre ->
                    GenreChip(
                        genreName = genre.name,
                        textStyle = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            //#endregion

            //#region WATCHLIST BUTTON
            // WATCHLIST BUTTON
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedWatchlistButton(
                    onWatchlistClicked = { onWatchlistClicked() },
                    isTitleWatchlisted = title.isWatchlisted,
                    contentPadding = ButtonDefaults.ContentPadding,
                    textStyle = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    buttonShape = MaterialTheme.shapes.small
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            //#endregion

            //#region OVERVIEW
            val titleOverview = title.overview
            if (titleOverview != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeadline(
                        label = stringResource(id = R.string.details_summary_label),
                        modifier = Modifier.weight(1f)
                    )
                    if (showExpandSummaryArrow) {
                        IconButton(
                            onClick = { expandedSummaryState = !expandedSummaryState },
                            modifier = Modifier.rotate(rotationState)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = stringResource(id = R.string.cd_expand_more)
                            )
                        }
                    }
                }
                Text(
                    text = titleOverview,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    maxLines = if (expandedSummaryState) Int.MAX_VALUE else MAX_SUMMARY_LINES,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = {
                        if (it.hasVisualOverflow && showExpandSummaryArrow == false) {
                            showExpandSummaryArrow = true
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            //#endregion

            //#region EXTRA DETAILS - the headline label is within the ExtraDetailsSection composable
            ExtraDetailsSection(title = title)
            Spacer(modifier = Modifier.height(12.dp))
            //#endregion

            //#region CAST
            val cast = title.cast
            if (cast != null) {
                SectionHeadline(label = stringResource(id = R.string.details_cast_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(
                        key = { castMember -> castMember.id },
                        items = cast
                    ) { castMember: CastMember ->
                        CastMemberCard(
                            castMember = castMember,
                            placeHolderPortrait = placeHolderPortrait
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            //#endregion

            //#region VIDEOS
            AnimatedVisibility(
                visible = videos != null,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
            ) {
                /* Non-null assertion because AnimatedVisibility already does the null check */
                VideosSection(
                    videos = videos!!,
                    player = player,
                    onVideoSelected = onVideoSelected,
                    playerState = playerState,
                    placeHolderBackdrop = placeHolderBackdrop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            //#enregion

            //#region SIMILAR
            if (title.similar != null) {
                SectionHeadline(label = stringResource(id = R.string.details_similar_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(
                        /* Non-null assertion because we check for null before */
                        key = { titleItemMinimal -> titleItemMinimal.mediaId },
                        items = title.similar!!
                    ) { titleItemMinimal: TitleItemMinimal ->
                        TitleMinimalCard(
                            titleItemMinimal = titleItemMinimal,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onSimilarOrRecommendedTitleClicked
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            //#endregion

            //#region RECOMMENDATIONS
            if (title.recommendations != null) {
                SectionHeadline(label = stringResource(id = R.string.details_recommended_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(
                        key = { titleItemMinimal -> titleItemMinimal.mediaId },
                        /* Non-null assertion because we check for null before */
                        items = title.recommendations!!
                    ) { titleItemMinimal: TitleItemMinimal ->
                        TitleMinimalCard(
                            titleItemMinimal = titleItemMinimal,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onSimilarOrRecommendedTitleClicked
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            //#endregion
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentPadding.calculateBottomPadding())
        )
    }
}

@Composable
fun DetailsInfoRow(
    title: Title,
    titleType: TitleType,
    runtimeOrSeasonsString: String,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    val releaseDate = title.releaseDate
    val releaseDateString = if (releaseDate == null) {
        stringResource(id = R.string.details_data_notavailable)
    } else {
        when (titleType) {
            TitleType.MOVIE -> {
                DateFormatter.getLocalizedShortDateString(releaseDate)
            }
            TitleType.TV -> {
                val tv = title as TV
                if (tv.status == TvStatus.Ended || tv.status == TvStatus.Cancelled) {
                    val lastAirDate = tv.lastAirDate
                    if (lastAirDate != null) {
                        "${releaseDate.year} - ${lastAirDate.year}"
                    } else {
                        "${releaseDate.year}"
                    }
                } else {
                    "${releaseDate.year} -"
                }
            }
        }
    }

    Row(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Outlined.Today, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = releaseDateString,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = runtimeOrSeasonsString, style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = IMDBOrange)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(
                    id = R.string.title_item_vote_score,
                    "%.1f".format(title.voteAverage)
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun CastMemberCard(
    castMember: CastMember,
    placeHolderPortrait: Painter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 150.dp, height = 290.dp)
            .padding(bottom = 5.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(castMember.pictureLink)
                .crossfade(true)
                .build(),
            placeholder = placeHolderPortrait,
            fallback = placeHolderPortrait,
            error = placeHolderPortrait,
            contentDescription = null, //Decorative
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = castMember.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = castMember.character,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleMinimalCard(
    titleItemMinimal: TitleItemMinimal,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemMinimal) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 150.dp, height = 300.dp)
            .padding(bottom = 5.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onTitleClicked(titleItemMinimal) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(titleItemMinimal.posterLink)
                .crossfade(true)
                .build(),
            placeholder = placeholderPoster,
            fallback = placeholderPoster,
            error = placeholderPoster,
            contentDescription = null, //Decorative
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 3.dp, bottom = 3.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = titleItemMinimal.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (titleItemMinimal.releaseDate != null) {
                    Text(
                        text = titleItemMinimal.releaseDate.year.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = IMDBOrange,
                    modifier = Modifier.scale(0.8f).padding(top = 2.dp)
                )
                Text(
                    text = "%.1f".format(titleItemMinimal.voteAverage),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeadline(label: String, modifier: Modifier = Modifier, bottomPadding: Dp = 5.dp) {
    Text(
        text = label,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier.padding(bottom = bottomPadding)
    )
}


@Preview
@Composable
fun CastMemberCardPreview() {
    val castMember =
        CastMember(id = 1, name = "Carrie-Anne Moss", character = "Trinity", pictureLink = null)
    val placeHolderPortrait = painterResource(id = R.drawable.placeholder_portrait_light)
    CastMemberCard(castMember = castMember, placeHolderPortrait = placeHolderPortrait)
}

//TODO: Remove once not needed anymore
private fun getMovieForTesting(): Movie {
    val genres = listOf(Genre(0, "Action"), Genre(1, "Science Fiction"), Genre(2, "Adventure"))
    val cast = listOf(
        CastMember(0, "Keanu Reaves", "Neo", null),
        CastMember(1, "Laurence Fishburne", "Morpheus", null),
        CastMember(2, "Carrie-Anne Moss", "Trinity", null),
        CastMember(3, "Hugo Weaving", "Agent Smith", null),
        CastMember(4, "Joe Pantoliano", "Cypher", null),
    )
    val videos = listOf(
        YtVideo(
            videoId = "nUEQNVV3Gfs",
            link = "https://www.youtube.com/watch?v=nUEQNVV3Gfs",
            name = "",
            type = YtVideoType.Trailer
        ),
        YtVideo(
            videoId = "RZ-MXBjvA38",
            link = "https://www.youtube.com/watch?v=RZ-MXBjvA38",
            name = "",
            type = YtVideoType.Teaser
        ),
        YtVideo(
            videoId = "L0fw0WzFaBM",
            link = "https://www.youtube.com/watch?v=L0fw0WzFaBM",
            name = "",
            type = YtVideoType.BehindTheScenes
        ),
        YtVideo(
            videoId = "m8e-FF8MsqU",
            link = "https://www.youtube.com/watch?v=m8e-FF8MsqU",
            name = "",
            type = YtVideoType.Featurette
        )
    )
    return Movie(
        id = 603,
        name = "The Matrix",
        imdbId = "tt0133093",
        overview = "Set in the 22nd century, The Matrix tells the story of a computer hacker who joins a group of underground insurgents fighting the vast and powerful computers who now rule the earth.",
        tagline = "Welcome to the Real World.",
        posterLink = "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
        backdropLink = "https://image.tmdb.org/t/p/w500/l4QHerTSbMI7qgvasqxP36pqjN6.jpg",
        genres = genres,
        cast = cast,
        videos = videos,
        status = MovieStatus.Released,
        releaseDate = LocalDate.parse("1999-03-30"),
        revenue = 463517383,
        runtime = 136,
        voteCount = 22622,
        voteAverage = 8.195,
        isWatchlisted = false,
        recommendations = null,
        similar = null
    )
}

//TODO: Remove once not needed anymore
private fun getTvForTesting(): TV {
    val genres = listOf(Genre(0, "Crime"), Genre(1, "Drama"))
    val cast = listOf(
        CastMember(
            0,
            "Giancarlo Esposito",
            "Leo Pap",
            "https://image.tmdb.org/t/p/w500/lBvDQZjxhIGMbH61iHnqerpbqHc.jpg"
        ),
        CastMember(
            1,
            "Paz Vega",
            "Ava Mercer",
            "https://image.tmdb.org/t/p/w500/fNLlJysFd5f0Q8Lj20EZpU8BiRN.jpg"
        ),
        CastMember(
            2,
            "Rufus Sewell",
            "Roger Salas",
            "https://image.tmdb.org/t/p/w500/yc2EWyg45GO03YqDttaEhjvegiE.jpg"
        ),
        CastMember(
            3,
            "Tati Gabrielle",
            "Hannah Kim",
            "https://image.tmdb.org/t/p/w500/zDtHNX7vXfhRmN2U5Ffmd9mLlo0.jpg"
        ),
        CastMember(
            4,
            "Peter Mark Kendall",
            "Stan Loomis",
            "https://image.tmdb.org/t/p/w500/9Cj5ySZ6znkNcASB5CZeibuDGsd.jpg"
        ),
    )
    val videos = listOf(
        YtVideo(
            videoId = "nUEQNVV3Gfs",
            link = "https://www.youtube.com/watch?v=nUEQNVV3Gfs",
            name = "",
            type = YtVideoType.Trailer
        ),
        YtVideo(
            videoId = "RZ-MXBjvA38",
            link = "https://www.youtube.com/watch?v=RZ-MXBjvA38",
            name = "",
            type = YtVideoType.Teaser
        ),
        YtVideo(
            videoId = "L0fw0WzFaBM",
            link = "https://www.youtube.com/watch?v=L0fw0WzFaBM",
            name = "",
            type = YtVideoType.BehindTheScenes
        ),
        YtVideo(
            videoId = "m8e-FF8MsqU",
            link = "https://www.youtube.com/watch?v=m8e-FF8MsqU",
            name = "",
            type = YtVideoType.Featurette
        )
    )
    return TV(
        id = 156902,
        name = "Kaleidoscope",
        overview = "A master criminal and his crew hatch an elaborate scheme to break into a secure vault, but are forced to pivot when things don't go according to plan.",
        tagline = "There are 7 billion ways to solve a crime.",
        posterLink = "https://image.tmdb.org/t/p/w500/2nXJoSB5Y6R9ne7pjqL7Cs3zqY1.jpg",
        backdropLink = "https://image.tmdb.org/t/p/w500/kSqEenES71d1ApF2rRWxp5X0en5.jpg",
        genres = genres,
        cast = cast,
        videos = videos,
        status = TvStatus.Ended,
        releaseDate = LocalDate.parse("2023-01-01"),
        lastAirDate = LocalDate.parse("2023-01-01"),
        numberOfSeasons = 1,
        numberOfEpisodes = 9,
        voteCount = 97,
        voteAverage = 7.397,
        isWatchlisted = true,
        recommendations = null,
        similar = null
    )
}

