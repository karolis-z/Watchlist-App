package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.ui.theme.IMDBOrange
import com.myapplications.mywatchlist.ui.theme.dark_IMDBOrange
import com.myapplications.mywatchlist.ui.theme.light_IMDBOrange
import java.time.LocalDate

@Composable
fun TitleItemCard(
    title: TitleItem,
    placeholderImage: Painter,
    onWatchlistClicked: () -> Unit, // TODO: Not using this for now while testing
    modifier: Modifier = Modifier
) {
    var isWatchlisted by remember {
        mutableStateOf(title.isWatchlisted)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 10.dp)
            .clickable {

            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Column with poster
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(title.posterLink)
                        .crossfade(true)
                        .build(),
                    placeholder = placeholderImage,
                    fallback = placeholderImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Column with all Title info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 7.dp, horizontal = 10.dp)
            )
            {
                Row {
                    AutoResizedText(
                        text = title.name,
                        textStyle = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(key = { genre -> genre.id }, items = title.genres) { genre: Genre ->
                        GenreChip(genreName = genre.name)
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = title.overview ?: "",
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) IMDBOrange else IMDBOrange
                    )
                    Text(
                        text = stringResource(
                            id = R.string.title_item_vote_score,
                            title.voteAverage.toString()
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedVisibility(
                        visible = isWatchlisted,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                    ) {
                        WatchlistedButton(
                            onWatchlistClicked = {
                                onWatchlistClicked.invoke()
                                isWatchlisted = !isWatchlisted
                            },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = !isWatchlisted,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                    ) {
                        WatchlistButton(
                            onWatchlistClicked = {
                                onWatchlistClicked.invoke()
                                isWatchlisted = !isWatchlisted
                            },
                            modifier = Modifier.height(30.dp)
                        )
                    }
//                    if (title.isWatchlisted) {
//                        WatchlistedButton(
//                            onWatchlistClicked = onWatchlistClicked,
//                            modifier = Modifier.height(30.dp)
//                        )
//                    } else {
//                        WatchlistButton(
//                            onWatchlistClicked = onWatchlistClicked,
//                            modifier = Modifier.height(30.dp)
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistButton(onWatchlistClicked: () -> Unit, modifier: Modifier = Modifier) {

    val buttonContainerColor = if (isSystemInDarkTheme()) dark_IMDBOrange else light_IMDBOrange

    Button(
        onClick = onWatchlistClicked,
        contentPadding = PaddingValues(vertical = 0.dp, horizontal = 10.dp),
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = buttonContainerColor)
    ) {
        Text(
            text = stringResource(id = R.string.title_item_bookmark),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            imageVector = Icons.Outlined.BookmarkBorder,
            contentDescription = stringResource(id = R.string.cd_watchlist_button_watchlist),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }
}

@Composable
fun WatchlistedButton(onWatchlistClicked: () -> Unit, modifier: Modifier = Modifier) {

    val buttonContainerColor = if (isSystemInDarkTheme()) dark_IMDBOrange else light_IMDBOrange

    OutlinedButton(
        onClick = onWatchlistClicked,
        contentPadding = PaddingValues(vertical = 0.dp, horizontal = 10.dp),
        modifier = modifier,
        border = BorderStroke(
            width = ButtonDefaults.outlinedButtonBorder.width,
            color = buttonContainerColor
        ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Text(
            text = stringResource(id = R.string.title_item_bookmarked),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            imageVector = Icons.Filled.Bookmark,
            contentDescription = stringResource(id = R.string.cd_watchlist_button_watchlisted),
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = buttonContainerColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TitleItemCardPreview() {
    TitleItemCard(
        title = TitleItem(
            id = 0,
            name = "Matrix Resurrections",
            type = TitleType.MOVIE,
            mediaId = 0,
            overview = "Plagued by strange memories, Neo's life takes an unexpected turn when he finds himself back inside the Matrix",
            posterLink = null,
            genres = listOf(Genre(0, "Action"), Genre(1, "Science Fiction"), Genre(2, "Adventure")),
            releaseDate = LocalDate.now(),
            voteCount = 10000,
            voteAverage = 8.8,
            isWatchlisted = true
        ),
        onWatchlistClicked = {},
        placeholderImage = painterResource(id = R.drawable.placeholder_poster_light),
    )
}

@Preview
@Composable
fun WatchlistButtonPreviewNotWatchlisted() {
    WatchlistedButton(onWatchlistClicked = {})
}

@Preview
@Composable
fun WatchlistButtonPreviewWatchlisted() {
    WatchlistButton(onWatchlistClicked = {})
}