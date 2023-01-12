package com.myapplications.mywatchlist.ui.details

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.DateFormatter
import com.myapplications.mywatchlist.domain.entities.CastMember
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.MovieStatus
import com.myapplications.mywatchlist.ui.components.AnimatedWatchlistButton
import com.myapplications.mywatchlist.ui.components.GenreChip
import com.myapplications.mywatchlist.ui.theme.IMDBOrange
import java.time.LocalDate

@Composable
fun DetailsScreen(
    titleId: Long,
    titleType: String,
) {

    val viewModel = hiltViewModel<DetailsViewModel>()
    viewModel.getTitle(titleId, titleType)
    val uiMovieState = viewModel.movieUiState.collectAsState()
    val uiTvState = viewModel.tvUiState.collectAsState()

    val movie = uiMovieState.value
    val tv = uiTvState.value

    if (movie == null) {
        return
    }

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

    var runtimeString: String? = null
    if (movie.runtime != null) {
        val hoursAndMinutesPair = viewModel.convertRuntimeToHourAndMinutesPair(movie.runtime)
        runtimeString = stringResource(
            id = R.string.details_runtime,
            hoursAndMinutesPair.first,
            hoursAndMinutesPair.second
        )
    }

    DetailsScreenContent(
        movie = movie,
        placeHolderBackdrop = placeholderImage,
        runtimeString = runtimeString,
        placeHolderPortrait = placeHolderPortrait
    )

}

@Composable
fun DetailsScreenContent(
    movie: Movie,
    runtimeString: String?,
    placeHolderBackdrop: Painter,
    placeHolderPortrait: Painter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DetailsBackdrop(
            movie = movie,
            placeholderImage = placeHolderBackdrop,
            modifier = Modifier.height(300.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailsInfoRow(
                movie = movie,
                modifier = Modifier.fillMaxWidth(),
                runtimeString = runtimeString
            )
            Spacer(modifier = Modifier.height(15.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(key = { genre -> genre.id }, items = movie.genres) { genre: Genre ->
                    GenreChip(
                        genreName = genre.name,
                        textStyle = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(7.dp))
            // TODO: Change to Animated Visibility? Some titles might not have an overview.
            if (movie.overview != null) {
                SectionHeadline(label = stringResource(id = R.string.details_summary_label))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Cast
            if (movie.cast != null) {
                SectionHeadline(label = stringResource(id = R.string.details_cast_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(
                        /* Combining a key out of cast member name and character name to make sure
                        we have a unique value */
                        key = { castMember -> "${castMember.name}-${castMember.character}" },
                        items = movie.cast
                    ) { castMember: CastMember ->
                        CastMemberCard(
                            castMember = castMember,
                            placeHolderPortrait = placeHolderPortrait
                        )
                    }
                }
            }

            // Watchlist Button
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                AnimatedWatchlistButton(
                    onWatchlistClicked = { /*TODO*/ },
                    isTitleWatchlisted = movie.isWatchlisted,
                    contentPadding = ButtonDefaults.ContentPadding,
                    textStyle = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DetailsInfoRow(
    movie: Movie,
    runtimeString: String?,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    Row(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Outlined.Today, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            // TODO: Implement proper handling of possibly null release date
            Text(
                text = DateFormatter.getLocalizedShortDateString(movie.releaseDate!!),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            // TODO: Implement proper handling of possibly null runtime
            Text(text = runtimeString ?: "", style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = IMDBOrange)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(
                    id = R.string.title_item_vote_score,
                    "%.1f".format(movie.voteAverage)
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun DetailsBackdrop(
    movie: Movie,
    placeholderImage: Painter,
    modifier: Modifier = Modifier,
    height: Dp = 250.dp
) {
    Box(
        modifier = modifier
            .height(height)
//            .offset(y = (-64).dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.backdropLink)
                .crossfade(true)
                .build(),
            placeholder = placeholderImage,
            fallback = placeholderImage,
            contentDescription = null, //Decorative
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = movie.name,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.background,
                    offset = Offset.Zero,
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 10.dp)
        )
    }
}

@Composable
fun CastMemberCard(
    castMember: CastMember,
    placeHolderPortrait: Painter,
    modifier: Modifier = Modifier
){ 
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

@Preview()
@Composable
fun CastMemberCardPreview(){
    val castMember = CastMember(name = "Carrie-Anne Moss", character = "Trinity", pictureLink = null)
    val placeHolderPortrait = painterResource(id = R.drawable.placeholder_portrait_light)
    CastMemberCard(castMember = castMember, placeHolderPortrait = placeHolderPortrait)
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DetailsScreenPreview(){
    val genres = listOf(Genre(0, "Action"), Genre(1, "Science Fiction"), Genre(2, "Adventure"))
    val cast = listOf(
        CastMember("Keanu Reaves", "Neo", null),
        CastMember("Laurence Fishburne", "Morpheus", null),
        CastMember("Carrie-Anne Moss", "Trinity", null),
        CastMember("Hugo Weaving", "Agent Smith", null),
        CastMember("Joe Pantoliano", "Cypher", null),
    )
    val videos = listOf(
        "https://www.youtube.com/watch?v=nUEQNVV3Gfs",
        "https://www.youtube.com/watch?v=RZ-MXBjvA38",
        "https://www.youtube.com/watch?v=L0fw0WzFaBM",
        "https://www.youtube.com/watch?v=m8e-FF8MsqU"
    )
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
        movie = Movie(
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
        ),
        placeHolderBackdrop = placeholderImage,
        runtimeString = "2h 16 min",
        placeHolderPortrait = placeHolderPortrait
    )
}