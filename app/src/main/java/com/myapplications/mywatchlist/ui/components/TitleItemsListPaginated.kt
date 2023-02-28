package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.entities.UiError
import com.myapplications.mywatchlist.ui.titlelist.AppendPrependError
import com.myapplications.mywatchlist.ui.titlelist.AppendPrependLoading
import com.myapplications.mywatchlist.ui.titlelist.FullScreenLoadingCircle
import com.myapplications.mywatchlist.ui.titlelist.pagingLoadStateItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleItemsListPaginated(
    titles: LazyPagingItems<TitleItemFull>,
    error: (Throwable) -> UiError,
    errorComposable: @Composable (error: UiError) -> Unit,
    onWatchlistClicked: (TitleItemFull) -> Unit,
    onTitleClicked: (TitleItemFull) -> Unit,
    placeHolderPoster: Painter,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {

    Crossfade(targetState = titles.loadState.refresh, modifier = modifier) { loadState ->
        when (loadState) {
            LoadState.Loading -> {
                FullScreenLoadingCircle()
            }
            is LoadState.Error -> {
                errorComposable(error = error(loadState.error))
            }
            is LoadState.NotLoading -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    state = listState
                ) {
                    // Prepend load or error item
                    pagingLoadStateItem(
                        loadState = titles.loadState.prepend,
                        keySuffix = "prepend",
                        loading = { AppendPrependLoading() },
                        error = {
                            AppendPrependError(
                                errorText = "Could not load more data",
                                onButtonRetryClick = { titles.retry() }
                            )
                        }
                    )

                    // Main Content
                    items(
                        items = titles,
                        key = { titleItem -> titleItem.id }
                    ) { titleItem: TitleItemFull? ->
                        titleItem?.let { titleItemFull ->
                            TitleItemCard(
                                title = titleItemFull,
                                onWatchlistClicked = { onWatchlistClicked(titleItemFull) },
                                onTitleClicked = { onTitleClicked(it) },
                                placeholderImage = placeHolderPoster,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }

                    // Append load or error item
                    pagingLoadStateItem(
                        loadState = titles.loadState.append,
                        keySuffix = "append",
                        loading = { AppendPrependLoading() },
                        error = {
                            AppendPrependError(
                                errorText = "Could not load more data",
                                onButtonRetryClick = { titles.retry() }
                            )
                        }
                    )
                }
            }
        }
        LaunchedEffect(key1 = loadState is LoadState.NotLoading) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
}