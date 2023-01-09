package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.domain.entities.TitleItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleItemsList(
    titleItems: List<TitleItem>,
    placeholderImage: Painter,
    onWatchlistClicked: (TitleItem) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        verticalArrangement = verticalArrangement,
        contentPadding = contentPadding,
        state = state,
        modifier = modifier
    ) {
        items(
            items = titleItems,
            key = { titleItem -> "${titleItem.type.name}-${titleItem.mediaId}" }) { item: TitleItem ->
            TitleItemCard(
                title = item,
                onWatchlistClicked = { onWatchlistClicked(item) },
                placeholderImage = placeholderImage,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}