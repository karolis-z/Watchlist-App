package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import com.myapplications.mywatchlist.ui.theme.*
import java.time.LocalDate

@Composable
fun TitleItemCard(
    title: TitleItem,
    placeholderImage: Painter,
    onWatchlistClicked: () -> Unit,
    onTitleClicked: (TitleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 10.dp)
            .clickable {
                onTitleClicked(title)
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
                            "%.1f".format(title.voteAverage)
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedWatchlistButton(
                        onWatchlistClicked = onWatchlistClicked,
                        isTitleWatchlisted = title.isWatchlisted,
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedWatchlistButton(
    onWatchlistClicked: () -> Unit,
    isTitleWatchlisted: Boolean,
    modifier: Modifier = Modifier
) {
    val buttonPrimaryColor = if (isSystemInDarkTheme()) dark_IMDBOrange else light_IMDBOrange
    val textColorWatchlisted = MaterialTheme.colorScheme.onBackground
    val textColorNotWatchlisted = if (isSystemInDarkTheme()) dark_onIMDBOrange else light_onIMDBOrange

    var isWatchListed by remember {
        mutableStateOf(isTitleWatchlisted)
    }
    val transition = updateTransition(isWatchListed, label = "button_transitions")
    val containerColor by transition.animateColor(label = "container_color") { watchlisted ->
        if (watchlisted) Color.Transparent else buttonPrimaryColor
    }
    val borderColor by transition.animateColor(label = "border_color") { watchlisted ->
        if (watchlisted) buttonPrimaryColor else Color.Transparent
    }
    val textColor by transition.animateColor(label = "text_coolr") { watchlisted ->
        if (watchlisted) textColorWatchlisted else textColorNotWatchlisted
    }

    Button(
        onClick = {
            onWatchlistClicked()
            isWatchListed = !isWatchListed
        },
        contentPadding = PaddingValues(vertical = 0.dp, horizontal = 10.dp),
        modifier = modifier.animateContentSize(),
        border = BorderStroke(
            width = ButtonDefaults.outlinedButtonBorder.width,
            color = borderColor
        ),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(
            text = if (isWatchListed) {
                stringResource(id = R.string.title_item_bookmarked)
            } else {
                stringResource(id = R.string.title_item_bookmark)
            },
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Crossfade(
            targetState = isWatchListed,
            animationSpec = tween(easing = LinearEasing)
        ) { watchlisted ->
            when(watchlisted){
                true -> Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = stringResource(id = R.string.cd_watchlist_button_watchlisted),
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = buttonPrimaryColor
                )
                false -> Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(id = R.string.cd_watchlist_button_watchlist),
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = textColorNotWatchlisted
                )
            }
        }
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
        onTitleClicked = {},
        placeholderImage = painterResource(id = R.drawable.placeholder_poster_light),
    )
}