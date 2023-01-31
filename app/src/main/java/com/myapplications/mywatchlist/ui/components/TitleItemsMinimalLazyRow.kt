package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.domain.entities.TitleItemMinimal


@Composable
fun TitleItemsMinimalLazyRow(
    titleItemsMinimal: List<TitleItemMinimal>,
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemMinimal) -> Unit,
    modifier: Modifier = Modifier
){
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = modifier) {
        items(
            key = { titleItemMinimal -> titleItemMinimal.mediaId },
            items = titleItemsMinimal
        ) { titleItemMinimal: TitleItemMinimal ->
            TitleMinimalCard(
                titleItemMinimal = titleItemMinimal,
                placeholderPoster = placeholderPoster,
                onTitleClicked = onTitleClicked
            )
        }
    }
}