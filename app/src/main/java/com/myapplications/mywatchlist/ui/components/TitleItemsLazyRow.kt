package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleItemMinimal

@JvmName("TitleItemsLazyRow1")
@Composable
fun TitleItemsLazyRow(
    titleItemsFull: List<TitleItemFull>,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = modifier) {
        items(
            key = { titleItemFull -> titleItemFull.mediaId },
            items = titleItemsFull
        ) { titleItemFull: TitleItemFull ->
            TitleItemVerticalCard(
                titleItem = titleItemFull,
                placeholderPoster = placeholderPoster,
                onTitleClicked = onTitleClicked
            )
        }
    }
}

@Composable
fun TitleItemsLazyRow(
    titleItemsMinimal: List<TitleItemMinimal>,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemMinimal) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = modifier) {
        items(
            key = { titleItemMinimal -> titleItemMinimal.mediaId },
            items = titleItemsMinimal
        ) { titleItemMinimal: TitleItemMinimal ->
            TitleItemVerticalCard(
                titleItem = titleItemMinimal,
                placeholderPoster = placeholderPoster,
                onTitleClicked = onTitleClicked
            )
        }
    }
}