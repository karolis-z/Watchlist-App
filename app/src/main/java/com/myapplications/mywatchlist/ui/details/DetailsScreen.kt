package com.myapplications.mywatchlist.ui.details

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.ui.details.management.states.toolbar.ToolbarState
import com.myapplications.mywatchlist.ui.details.management.states.toolbar.scrollflags.ExitUntilCollapsedState

//private val ContentPadding = 8.dp
//private val Elevation = 4.dp
//private val ButtonSize = 24.dp
//private const val Alpha = 0.75f
//
//private val ExpandedPadding = 1.dp
//private val CollapsedPadding = 3.dp
//
//private val ExpandedCostaRicaHeight = 20.dp
//private val CollapsedCostaRicaHeight = 16.dp
//
//private val ExpandedWildlifeHeight = 32.dp
//private val CollapsedWildlifeHeight = 24.dp

//private val MapHeight = CollapsedCostaRicaHeight * 2

private val MinToolbarHeight = 64.dp

private val NavButtonGradientSize = 40.dp

// This considers top padding for a 48x48dp IconButton. This size is for accessibility
// start = end = ((standard total width (16pad + 24 icon + 16pad)=56) - 48)/2 = 4.dp
private val NavigationIconPadding =
    PaddingValues(
        start = 4.dp,
        top = (MinToolbarHeight - 48.dp) / 2,
        end = 4.dp,
        bottom = (MinToolbarHeight - 48.dp) / 2
    )

private val StandardHzPadding = 16.dp
private val ExpandedStateTitlePadding =
    PaddingValues(start = StandardHzPadding, top = 0.dp, end = 0.dp, bottom = 10.dp)

private const val TAG = "DETAILS_SCREEN_TEST"

@Composable
fun DetailsScreen(
    placeHolderBackdrop: Painter,
    placeHolderPortrait: Painter,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculating the toolbar height, by the expected image's aspect ratio and the screen width
    val maxToolbarHeight =
        (LocalConfiguration.current.screenWidthDp / Constants.BACKDROP_IMAGE_ASPECT_RATIO).dp

    val toolbarHeightRange = with(LocalDensity.current) {
        MinToolbarHeight.roundToPx()..maxToolbarHeight.roundToPx()
    }
    val scrollState = rememberScrollState()
    val toolbarState = rememberToolbarState(toolbarHeightRange = toolbarHeightRange)
    toolbarState.scrollValue = scrollState.value


    Box(modifier = modifier) {
        FakeContentForScrollingInBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = StandardHzPadding),
            scrollState = scrollState,
            contentPadding = PaddingValues(top = maxToolbarHeight)
        )
        MyCollapsingToolbar(
            progress = toolbarState.progress,
            onNavigateUp = onNavigateUp,
            placeHolderBackdrop = placeHolderBackdrop,
            maxToolbarHeight = maxToolbarHeight,
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { toolbarState.height.toDp() })
                .graphicsLayer { translationY = toolbarState.offset }
        )
    }
}

@Composable
private fun rememberToolbarState(toolbarHeightRange: IntRange): ToolbarState {
    return rememberSaveable(saver = ExitUntilCollapsedState.saver) {
        ExitUntilCollapsedState(toolbarHeightRange)
    }
}

@Composable
fun FakeContentForScrollingInBox(
    scrollState: ScrollState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.verticalScroll(scrollState)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentPadding.calculateTopPadding())
        )
        repeat(5) {
            Text(text = "Text #$it", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp), style = MaterialTheme.typography.bodyMedium)
            Image(painter = painterResource(id = R.drawable.placeholder_backdrop_dark), contentDescription = null, contentScale = ContentScale.Fit)
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentPadding.calculateBottomPadding())
        )
    }

}





@Composable
fun MyCollapsingToolbar(
    progress: Float,
    onNavigateUp: () -> Unit,
    maxToolbarHeight: Dp,
    placeHolderBackdrop: Painter,
    modifier: Modifier = Modifier
) {
//    val costaRicaHeight = with(LocalDensity.current) {
//        lerp(CollapsedCostaRicaHeight.toPx(), ExpandedCostaRicaHeight.toPx(), progress).toDp()
//    }
//    val wildlifeHeight = with(LocalDensity.current) {
//        lerp(CollapsedWildlifeHeight.toPx(), ExpandedWildlifeHeight.toPx(), progress).toDp()
//    }
//    val logoPadding = with(LocalDensity.current) {
//        lerp(CollapsedPadding.toPx(), ExpandedPadding.toPx(), progress).toDp()
//    }
    var expandedToolbarHeight = with(LocalDensity.current) { maxToolbarHeight.roundToPx() }

    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Box (modifier = Modifier.fillMaxSize()) {
            //#region Background Image
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
                // TODO: Should use BiasAlignment or Center??
//                alignment = BiasAlignment(0f, 1f - ((1f - progress) * 0.75f)),
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .layoutId(CollapsingTopBarIds.IMAGE_ID)
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
                    .statusBarsPadding()
                    .fillMaxSize()
            ) {
                MyCollapsingToolbarLayout (progress = progress) {
                    //#region Navigate Up Button
                    IconButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.layoutId(CollapsingToolbarContent.NavUpButton)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(NavButtonGradientSize)
                                .align(Alignment.Center)
                                .background(
                                    color = Color.Black.copy(alpha = 0.15f),
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
                    //#endregion
                    Text(
                        text = "The Matrix Resurrections The Matrix Resurrections",
                        style =  MaterialTheme.typography.displaySmall.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.onBackground,
                                offset = Offset.Zero,
                                blurRadius = 1f
                            )
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier
                            .wrapContentHeight()
                            .wrapContentWidth()
                            .layoutId(CollapsingToolbarContent.TitleText)
                    )

                }
            }
        }
    }
}

@Composable
private fun MyCollapsingToolbarLayout(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val placeablesMap = mutableMapOf<Placeable, CollapsingToolbarContent>()
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        placeablesMap.clear()

        val contentItems = CollapsingToolbarContent.values().toList()
        check(measurables.size == contentItems.size) // [0]: UpIconButton | [1]: Text

        val direction = this.layoutDirection

        val navIconX = NavigationIconPadding.calculateStartPadding(direction).roundToPx()
        val navIconY = NavigationIconPadding.calculateTopPadding().roundToPx()
        val centerOfCollapsedY = MinToolbarHeight.roundToPx() / 2

        val expandedTitleStartOffset = ExpandedStateTitlePadding.calculateStartPadding(direction).roundToPx()
        val expandedTitleBottomOffset = ExpandedStateTitlePadding.calculateBottomPadding().roundToPx()

//        val placeables = measurables.map { it.measure(constraints) }
        val placeables = mutableListOf<Placeable>()

        measurables.forEachIndexed { index, measurable ->
            val placeable = when(measurable.layoutId as CollapsingToolbarContent) {
                CollapsingToolbarContent.NavUpButton -> {
                    measurable.measure(constraints)
                }
                CollapsingToolbarContent.TitleText -> {
                    measurable.measure(
                        /* Assuming here that 0 placeable will be the NavUpButton, but this can go
                        wrong if forgotten and missed in the future when adding other elements */
                        Constraints.fixedWidth(constraints.maxWidth - placeables[0].width -
                                navIconX * 2 - StandardHzPadding.roundToPx())
                    )
                }
            }
            placeablesMap[placeable] = measurable.layoutId as CollapsingToolbarContent
            placeables.add(placeable)
        }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {

            val upIcon = placeables[0]
            val title = placeables[1]
//
//            upIcon.placeRelative(x = navIconX, y = navIconY)

            // TODO: MUST FINISH ENSURING SAFETY WITH ENUMS

            val titleStartY = centerOfCollapsedY - title.height / 2
            val titleStopY = constraints.maxHeight - title.height - expandedTitleBottomOffset

            placeables.forEach { placeable ->
                when(placeablesMap[placeable]){
                    CollapsingToolbarContent.NavUpButton -> {
                        placeable.placeRelative(x = navIconX, y = navIconY)
                    }
                    CollapsingToolbarContent.TitleText -> {
                        placeable.placeRelative(
                            x = lerp(
                                start = upIcon.width + navIconX * 2,    // start = collapsed toolbar
                                stop =  expandedTitleStartOffset,       // end = expanded toolbar
                                fraction = progress
                            ),
                            y = lerp(
                                start = titleStartY,  // start = collapsed toolbar
                                stop = titleStopY,    // end = expanded toolbar
                                fraction = progress
                            )
                        )
                    }
                    null -> TODO()
                }
            }

            title.placeRelative(
                x = lerp(
                    start = upIcon.width + navIconX * 2,    // start = collapsed toolbar
                    stop =  expandedTitleStartOffset,       // end = expanded toolbar
                    fraction = progress
                ),
                y = lerp(
                    start = titleStartY,  // start = collapsed toolbar
                    stop = titleStopY,    // end = expanded toolbar
                    fraction = progress
                )
            )
        }
    }
}

enum class CollapsingToolbarContent {
    NavUpButton,
    TitleText
}

