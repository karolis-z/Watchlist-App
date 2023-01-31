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
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleItemsList(
    titleItemsFull: List<TitleItemFull>,
    placeholderImage: Painter,
    onWatchlistClicked: (TitleItemFull) -> Unit,
    onTitleClicked: (TitleItemFull) -> Unit,
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
            items = titleItemsFull,
            key = { titleItem -> "${titleItem.type.name}-${titleItem.mediaId}" }) { item: TitleItemFull ->
            TitleItemCard(
                title = item,
                onWatchlistClicked = { onWatchlistClicked(item) },
                onTitleClicked = { onTitleClicked(it) },
                placeholderImage = placeholderImage,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}