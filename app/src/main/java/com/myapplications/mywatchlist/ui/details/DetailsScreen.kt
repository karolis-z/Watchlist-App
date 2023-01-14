package com.myapplications.mywatchlist.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.DateFormatter
import com.myapplications.mywatchlist.domain.entities.*
import com.myapplications.mywatchlist.ui.components.AnimatedWatchlistButton
import com.myapplications.mywatchlist.ui.components.GenreChip
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.theme.IMDBOrange
import java.time.LocalDate

private const val TAG = "DETAILS_SCREEN"
private const val MAX_SUMMARY_LINES = 5

@Composable
fun DetailsScreen(
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
    var expandedSummaryState by remember { mutableStateOf(false)}
    var showExpandSummaryArrow by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expandedSummaryState) 180f else 0f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DetailsBackdrop(
            title = title,
            placeholderImage = placeHolderBackdrop,
            modifier = Modifier.height(300.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SectionHeadline(label = stringResource(id = R.string.details_summary_label), modifier = Modifier.weight(1f))
                    if (showExpandSummaryArrow){
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