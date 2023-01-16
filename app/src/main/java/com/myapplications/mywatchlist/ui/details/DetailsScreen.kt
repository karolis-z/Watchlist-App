package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.*
import androidx.compose.ui.layout.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.DateFormatter
import com.myapplications.mywatchlist.domain.entities.*
import com.myapplications.mywatchlist.ui.components.AnimatedWatchlistButton
import com.myapplications.mywatchlist.ui.components.GenreChip
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.BACK_ID
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.GRADIENT_ID
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.IMAGE_ID
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.TITLE_ID
import com.myapplications.mywatchlist.ui.theme.IMDBOrange
import java.time.LocalDate
import kotlin.math.roundToInt

private const val TAG = "DETAILS_SCREEN"
private const val MAX_SUMMARY_LINES = 5

@Composable
fun DetailsScreen(
    placeHolderBackdrop: Painter,
    placeHolderPortrait: Painter
) {

    var sizeImage by remember { mutableStateOf(IntSize.Zero) }.also {
        Log.d("IMAGE_SIZE", "DetailsScreen: sizeImage = ${it.value}")
    }
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
        startY = sizeImage.height.toFloat() / 3,  // 1/3
        endY = sizeImage.height.toFloat()
    )
    


    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
//            .verticalScroll(scrollState)
    ) {
        val (box, mainContent) = createRefs()

        val scrollState = rememberScrollState()
        val collapseRange = with(LocalDensity.current) { (370.dp - 64.dp).toPx() }
        Log.d(TAG, "collapseRange: $collapseRange ")
//    Log.d("TESTS", "scrollProvider: ${scrollProvider()} ")

        val collapseFractionProvider = {
            (scrollState.value / collapseRange).coerceIn(0f, 1f)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
//                .height(250.dp)
                .heightIn(min = 64.dp, max = 250.dp)
                .constrainAs(box){
                    top.linkTo(parent.top)
                }
        ) {


            CollapsingTopBar(
                collapseFractionProvider = collapseFractionProvider,
                modifier = Modifier
                    .statusBarsPadding()
//                .height(fullHeight)
                    .border(0.5.dp, Color.Red)
            ) {
                Icon(
                    modifier = Modifier
                        .wrapContentSize()
//                    .wrapContentHeight()
                        .layoutId(CollapsingTopBarIds.BACK_ID)
                        .clickable { }
                        .padding(16.dp),
//                    .border(0.5.dp, Color.Yellow),
                    imageVector = Icons.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(id = com.myapplications.mywatchlist.R.string.cd_back_arrow)
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
//                                    MaterialTheme.colorScheme.background
                                    Color.Transparent
                                ),
                                startY = sizeImage.height.toFloat() / 3,  // 1/3
                            )
                        )
                        .layoutId(GRADIENT_ID)
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://image.tmdb.org/t/p/w780/s16H6tpK2utvwDtzZ8Qy4qm5Emw.jpg")
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
                        .layoutId(IMAGE_ID)
                        .onGloballyPositioned {
                            sizeImage = it.size
                        }
                        .background(color = MaterialTheme.colorScheme.surface)
                        .graphicsLayer {
                            // When fully collapsed, the image will be invisible
                            alpha = 1 - collapseFractionProvider()
                        },
                )
                Text(
                    modifier = Modifier
                        .layoutId(CollapsingTopBarIds.TITLE_ID)
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp),
//                    .border(0.5.dp, Color.Blue),
                    text = "The Matrix",
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(mainContent){
                    top.linkTo(box.bottom)
                }
                .verticalScroll(scrollState)
//                .offset{
//                    IntOffset(x = 0, y = (collapseRange * collapseFractionProvider()).roundToInt())
//                }
        ) {
            FakeContentForScrolling(scrollState = scrollState)
        }

    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(scrollState)
//            .onGloballyPositioned {
////                Log.d(TAG, "DetailsScreen: size = ${it.size}")
//            }
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
////                .height(250.dp)
//                .heightIn(min = 64.dp, max = 250.dp)
//        ) {
//            CollapsingTopBar(
//                collapseFractionProvider = collapseFractionProvider,
//                modifier = Modifier
//                    .statusBarsPadding()
////                .height(fullHeight)
//                    .border(0.5.dp, Color.Red)
//            ) {
//                Icon(
//                    modifier = Modifier
//                        .wrapContentSize()
////                    .wrapContentHeight()
//                        .layoutId(CollapsingTopBarIds.BACK_ID)
//                        .clickable { }
//                        .padding(16.dp),
////                    .border(0.5.dp, Color.Yellow),
//                    imageVector = Icons.Filled.ArrowBack,
//                    tint = MaterialTheme.colorScheme.onSurface,
//                    contentDescription = stringResource(id = com.myapplications.mywatchlist.R.string.cd_back_arrow)
//                )
//                Box(
//                    Modifier
//                        .fillMaxSize()
//                        .background(
//                            brush = Brush.verticalGradient(
//                                colors = listOf(
//                                    Color.Transparent,
////                                    MaterialTheme.colorScheme.background
//                                    Color.Transparent
//                                ),
//                                startY = sizeImage.height.toFloat() / 3,  // 1/3
//                            )
//                        )
//                        .layoutId(GRADIENT_ID)
//                )
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data("https://image.tmdb.org/t/p/w780/s16H6tpK2utvwDtzZ8Qy4qm5Emw.jpg")
//                        .crossfade(true)
//                        .build(),
//                    placeholder = placeHolderBackdrop,
//                    fallback = placeHolderBackdrop,
//                    error = placeHolderBackdrop,
//                    contentDescription = null, //Decorative
//                    contentScale = ContentScale.FillWidth,
//                    alignment = Alignment.Center,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .layoutId(IMAGE_ID)
//                        .onGloballyPositioned {
//                            sizeImage = it.size
//                        }
//                        .background(color = MaterialTheme.colorScheme.surface)
//                        .graphicsLayer {
//                            // When fully collapsed, the image will be invisible
//                            alpha = 1 - collapseFractionProvider()
//                        },
//                )
//                Text(
//                    modifier = Modifier
//                        .layoutId(CollapsingTopBarIds.TITLE_ID)
//                        .wrapContentHeight()
//                        .padding(horizontal = 16.dp),
////                    .border(0.5.dp, Color.Blue),
//                    text = "The Matrix",
//                    style = MaterialTheme.typography.titleLarge,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(scrollState)
//        ) {
//
//            FakeContentForScrolling()
//        }
//    }


}

@Composable
fun CollapsingTopBar(
    modifier: Modifier = Modifier,
    collapseFractionProvider: () -> Float, // A value from (0-1) where 0 means fully expanded
    content: @Composable () -> Unit
) {
    val map = mutableMapOf<Placeable, Int>()
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val collapseFraction = collapseFractionProvider()
        Log.d(TAG, "CollapsingTopBar: collapseFraction = $collapseFraction. MaxHeight = ${constraints.maxHeight}. MinHeight = ${constraints.minHeight}. Total measurables count = ${measurables.count()}")
//        Log.d(TAG, "CollapsingTopBar: constraints = $constraints.. Layout direction: ${layoutDirection.name}")

        map.clear()
        val placeables = mutableListOf<Placeable>()

        val layoutHeight = lerp(250.dp, 64.dp, collapseFraction).roundToPx()

        measurables.map { measurable ->
            when (measurable.layoutId) {
                BACK_ID -> {
//                    Log.d(TAG, "CollapsingTopBar: BACK_ID measuring")
                    measurable.measure(constraints)
                }
                IMAGE_ID -> {
                    val imageHeight = layoutHeight
                    measurable.measure(Constraints.fixed(width = constraints.maxWidth, height = imageHeight))
                }
                GRADIENT_ID -> {
                    measurable.measure(Constraints.fixed(width = constraints.maxWidth, height = layoutHeight))
                }
                TITLE_ID -> {
                    val w = constraints.maxWidth - (collapseFraction * (placeables.first().width * 2)).toInt()
//                    Log.d(TAG, "CollapsingTopBar: TITLE_ID constraints width = $w")
                    measurable.measure(Constraints.fixedWidth(constraints.maxWidth - (collapseFraction * (placeables.first().width * 2)).toInt()))
                }

                else -> throw IllegalStateException("Id Not found")
            }.also { placeable ->
                map[placeable] = measurable.layoutId as Int
                placeables.add(placeable)
            }
        }

//        val layoutHeight = (constraints.maxHeight - constraints.minHeight * collapseFraction).toInt()

        Log.d(TAG, "CollapsingTopBar: CALCULATED HEIGHT = $layoutHeight")

        // Set the size of the layout as big as it can
        layout(
            width = constraints.maxWidth,
//            height = constraints.maxHeight
            height = layoutHeight
        ) {
            placeables.forEach { placeable ->
                when (map[placeable]) {
                    BACK_ID -> {
                        placeable.placeRelative(0, 0, zIndex = 2f)
                    }
                    IMAGE_ID -> placeable.placeRelative(0, 0)
                    GRADIENT_ID -> placeable.placeRelative(0, 0, zIndex = 1f)
                    TITLE_ID -> placeable.run {
                        val widthOffset = (placeables[0].width * collapseFraction).roundToInt()
                        val heightOffset = (placeables.first().height - placeable.height) / 2
                        Log.d(TAG, "TITLE_ID: First Placeable height ${placeables.first().height}. Title Placeable height = ${placeable.height}")
                        Log.d(TAG, "TITLE_ID: widthOffset= $widthOffset. Height offset = $heightOffset")
                        placeRelative(
                            widthOffset,
//                            if (collapseFraction == 1f) heightOffset else constraints.maxHeight - height
                            if (collapseFraction == 1f) heightOffset else layoutHeight - placeable.height
                        )
                    }
                }
            }
        }
    }
}

object CollapsingTopBarIds {
    const val BACK_ID = 1001
    const val SHARE_ID = 1002
    const val TITLE_ID = 1003
    const val IMAGE_ID = 1004
    const val GRADIENT_ID = 1005
    const val COLLAPSE_FACTOR = 0.6f
}

@Composable
fun FakeContentForScrolling(scrollState: ScrollState) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp)
//            .verticalScroll(scrollState)
    ) {
        repeat(5) {
            Text(text = "Text #$it", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp), style = MaterialTheme.typography.bodyMedium)
            Image(painter = painterResource(id = R.drawable.placeholder_backdrop_dark), contentDescription = null, contentScale = ContentScale.Fit)
        }
    }

}


@Composable
fun DetailsScreenOLD(
    placeHolderBackdrop: Painter,
    placeHolderPortrait: Painter
) {

    val viewModel = hiltViewModel<DetailsViewModel>()
    val uiState = viewModel.uiState.collectAsState()

    if (uiState.value.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingCircle()
        }
    } else if (uiState.value.isError) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.details_error),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        val title = uiState.value.title
        val type = uiState.value.type

        // Setting to data unavailable string, but will be replaced below if actually available
        var runtimeOrSeasonsString = stringResource(id = R.string.details_data_notavailable)
        if (title != null && type != null) {
            when (uiState.value.type) {
                TitleType.TV -> {
                    runtimeOrSeasonsString = pluralStringResource(
                        id = R.plurals.details_seasons,
                        count = (title as TV).numberOfSeasons,
                        (title as TV).numberOfSeasons
                    )
                }
                TitleType.MOVIE -> {
                    val runtime = (uiState.value.title as Movie).runtime
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
                else -> Unit // At this stage type should never be null
            }
            DetailsScreenContent(
                title = title,
                titleType = type,
                placeHolderBackdrop = placeHolderBackdrop,
                runtimeOrSeasonsString = runtimeOrSeasonsString,
                placeHolderPortrait = placeHolderPortrait,
                onWatchlistClicked = { viewModel.onWatchlistClicked() }
            )
        }
    }
}

@Composable
fun DetailsScreenContent(
    title: Title,
    titleType: TitleType,
    runtimeOrSeasonsString: String,
    placeHolderBackdrop: Painter,
    placeHolderPortrait: Painter,
    onWatchlistClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSummaryState by remember { mutableStateOf(false) }
    var showExpandSummaryArrow by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expandedSummaryState) 180f else 0f)

    val scrollState = rememberScrollState()



    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // TODO: Turning off temporarily the backdrop to test Collapsing TopBar
        DetailsCollapsingTopBar(
            fullHeight = 250.dp,
            scrollValue = { scrollState.value },
            title = title.name,
            onBackPressed = { },
            modifier = Modifier.fillMaxWidth()
        )
//        DetailsBackdrop(
//            title = title,
//            placeholderImage = placeHolderBackdrop,
//            modifier = Modifier.height(300.dp)
//        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
//                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // MAIN DETAILS
            DetailsInfoRow(
                title = title,
                titleType = titleType,
                modifier = Modifier.fillMaxWidth(),
                runtimeOrSeasonsString = runtimeOrSeasonsString
            )
            Spacer(modifier = Modifier.height(15.dp))

            // GENRES
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(key = { genre -> genre.id }, items = title.genres) { genre: Genre ->
                    GenreChip(
                        genreName = genre.name,
                        textStyle = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

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

            // OVERVIEW
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

            // EXTRA DETAILS - the headline label is within the ExtraDetailsSection composable
            ExtraDetailsSection(title = title)
            Spacer(modifier = Modifier.height(12.dp))

            // CAST
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
        }
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
fun DetailsBackdrop(
    title: Title,
    placeholderImage: Painter,
    modifier: Modifier = Modifier,
    height: Dp = 250.dp
) {
    var sizeImage by remember { mutableStateOf(IntSize.Zero) }
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
        startY = sizeImage.height.toFloat() / 3,  // 1/3
        endY = sizeImage.height.toFloat()
    )

    Box(
        modifier = modifier
            .height(height)
//            .offset(y = (-64).dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(title.backdropLink)
                .crossfade(true)
                .build(),
            placeholder = placeholderImage,
            fallback = placeholderImage,
            error = placeholderImage,
            contentDescription = null, //Decorative
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    sizeImage = it.size
                }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(gradient)
        )
        Text(
            text = title.name,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.onBackground,
                    offset = Offset.Zero,
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 10.dp)
        )
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DetailsScreenPreview() {

    val placeholderImage = if (isSystemInDarkTheme()) {
        painterResource(id = R.drawable.placeholder_backdrop_dark)
    } else {
        painterResource(id = R.drawable.placeholder_backdrop_light)
    }
    val placeHolderPortrait = if (isSystemInDarkTheme()) {
        painterResource(id = R.drawable.placeholder_portrait_dark)
    } else {
        painterResource(id = R.drawable.placeholder_portrait_light)
    }

    DetailsScreenContent(
        title = getMovieForTesting(),
        placeHolderBackdrop = placeholderImage,
        runtimeOrSeasonsString = "2h 16 min",
        placeHolderPortrait = placeHolderPortrait,
        onWatchlistClicked = {},
        titleType = TitleType.MOVIE
    )
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
        "https://www.youtube.com/watch?v=nUEQNVV3Gfs",
        "https://www.youtube.com/watch?v=RZ-MXBjvA38",
        "https://www.youtube.com/watch?v=L0fw0WzFaBM",
        "https://www.youtube.com/watch?v=m8e-FF8MsqU"
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
        isWatchlisted = false
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
        "https://www.youtube.com/watch?v=T92iINbl0t4",
        "https://www.youtube.com/watch?v=YbArSoOP8XQ",
        "https://www.youtube.com/watch?v=nHGk3sRxjYM"
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
        isWatchlisted = true
    )
}