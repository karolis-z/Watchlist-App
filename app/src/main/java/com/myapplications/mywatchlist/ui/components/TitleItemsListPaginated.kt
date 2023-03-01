package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.extensions.isScrollingUp
import com.myapplications.mywatchlist.core.util.extensions.pagingLoadStateItem
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.entities.UiError
import com.myapplications.mywatchlist.ui.titlelist.FullScreenLoadingCircle
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
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {

    val showScrollToTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 5
        }
    }

    Crossfade(targetState = titles.loadState.refresh, modifier = modifier) { loadState ->
        when (loadState) {
            LoadState.Loading -> {
                FullScreenLoadingCircle()
            }
            is LoadState.Error -> {
                errorComposable(error = error(loadState.error))
            }
            is LoadState.NotLoading -> {
                Box {
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
                    AnimatedVisibility(
                        visible = showScrollToTopButton && listState.isScrollingUp(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        enter = slideInVertically(
                            animationSpec = tween(easing = LinearEasing, durationMillis = 100),
                            initialOffsetY = { height -> height }),
                        exit = slideOutVertically(
                            animationSpec = tween(easing = LinearEasing, durationMillis = 100),
                            targetOffsetY = { height -> height })
                    ) {
                        ScrollToTopButton(
                            buttonLabel = stringResource(id = R.string.button_back_to_top),
                            scrollToTop = {
                                coroutineScope.launch {
                                    /* If the list is scrolled quite far down, the animated scroll
                                    to top can look jittery and laggy. So will use simple
                                    scrollToItem if the position is further down than 20 items */
                                    if (listState.firstVisibleItemIndex > 20) {
                                        listState.scrollToItem(0)
                                    } else {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollToTopButton(
    buttonLabel: String,
    scrollToTop: () -> Unit,
    modifier: Modifier = Modifier
) {
    SmallFloatingActionButton(
        onClick = scrollToTop,
        modifier = modifier.height(30.dp)
    ) {
        Row(
            modifier = modifier
                .wrapContentWidth()
                .padding(horizontal = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(id = R.string.cd_back_to_top)
            )
            Spacer(modifier = Modifier.width(5.dp))
            /* Adding 2.dp end padding because without it, the icon looks like it had slight more
             space before it than the text has after it. */
            Text(
                text = buttonLabel,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}

@Composable
fun AppendPrependError(
    errorText: String,
    onButtonRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    rowHorizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    errorTextStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = rowHorizontalArrangement) {
        ErrorText(
            errorMessage = errorText,
            onButtonRetryClick = onButtonRetryClick,
            errorTextStyle = errorTextStyle
        )
    }
}

@Composable
fun AppendPrependLoading(
    modifier: Modifier = Modifier,
    rowHorizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    circleSize: Dp = 40.dp
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = rowHorizontalArrangement) {
        LoadingCircle(modifier = Modifier.size(circleSize))
    }
}