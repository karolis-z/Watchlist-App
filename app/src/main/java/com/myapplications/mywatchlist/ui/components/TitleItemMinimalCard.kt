package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleItemMinimal
import com.myapplications.mywatchlist.ui.theme.IMDBOrange

@Composable
fun TitleItemVerticalCard(
    titleItem: TitleItemFull,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    modifier: Modifier = Modifier
) {
    BaseTitleItemVerticalCard(
        titleItem = titleItem,
        placeholderPoster = placeholderPoster,
        onTitleClicked = onTitleClicked as (TitleItem) -> Unit,
        modifier = modifier
    )
}

@Composable
fun TitleItemVerticalCard(
    titleItem: TitleItemMinimal,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemMinimal) -> Unit,
    modifier: Modifier = Modifier
) {
    BaseTitleItemVerticalCard(
        titleItem = titleItem,
        placeholderPoster = placeholderPoster,
        onTitleClicked = onTitleClicked as (TitleItem) -> Unit,
        modifier = modifier
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BaseTitleItemVerticalCard(
    titleItem: TitleItem,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 150.dp, height = 300.dp)
            .padding(bottom = 5.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onTitleClicked(titleItem) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(titleItem.posterLink)
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
                text = titleItem.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (titleItem.releaseDate != null) {
                    Text(
                        /* Non-null assertion because we already check for null above */
                        text = titleItem.releaseDate!!.year.toString(),
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
                    modifier = Modifier
                        .scale(0.8f)
                        .padding(top = 2.dp)
                )
                Text(
                    text = "%.1f".format(titleItem.voteAverage),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}