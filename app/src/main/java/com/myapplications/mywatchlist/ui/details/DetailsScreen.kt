package com.myapplications.mywatchlist.ui.details

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.ui.components.GenreChip

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

    Column(modifier = Modifier.fillMaxSize()) {
        DetailsBackdrop(
            movie = movie,
            placeholderImage = placeholderImage,
            modifier = Modifier.height(300.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailsInfoRow(movie = movie, modifier = Modifier.fillMaxWidth())
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
                Text(
                    text = stringResource(id = R.string.details_summary),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DetailsInfoRow(
    movie: Movie,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    Row(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Outlined.Today, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = movie.releaseDate.toString(), style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = movie.runtime.toString(), style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.wrapContentWidth()) {
            Icon(imageVector = Icons.Filled.Star, contentDescription = null)
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
    modifier: Modifier = Modifier.height(250.dp)
) {
    Box(modifier = modifier.offset(y = (-64).dp)) {
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