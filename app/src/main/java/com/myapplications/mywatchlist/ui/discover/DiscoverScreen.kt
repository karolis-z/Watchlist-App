package com.myapplications.mywatchlist.ui.discover

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.ErrorText
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList
import kotlinx.coroutines.launch

private const val TAG = "SEARCH_SCREEN"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DiscoverScreen(
    placeholderImage: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    modifier: Modifier
) {
    val viewModel = hiltViewModel<DiscoverViewModel>()
    val uiState = viewModel.uiState.collectAsState()
    val error = uiState.value.error

    // Remember a CoroutineScope to be able to scroll the list
    val coroutineScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)) {

            val isLoading = uiState.value.isLoading
            val isError = uiState.value.error != null
            val isTitlesAvailable = !uiState.value.titleItemsFull.isNullOrEmpty()
            val isNewSearch = !uiState.value.isSearchFinished && !uiState.value.isLoading

            SearchTextField(
                onSearchClicked = { viewModel.searchTitleClicked(it) },
                focusManager = focusManager,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = isNewSearch,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, bottom = 20.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.search_not_searched_yet),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingCircle()
                }
            }

            AnimatedVisibility(
                visible = isError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (error) {
                        DiscoverError.NO_INTERNET -> {
                            val errorMessage =
                                stringResource(id = R.string.error_no_internet_connection)
                            ErrorText(
                                errorMessage = errorMessage,
                                onButtonRetryClick = { viewModel.retrySearch() })
                        }
                        DiscoverError.FAILED_API_REQUEST -> {
                            val errorMessage =
                                stringResource(id = R.string.error_something_went_wrong)
                            ErrorText(
                                errorMessage = errorMessage,
                                onButtonRetryClick = { viewModel.retrySearch() })
                        }
                        DiscoverError.NOTHING_FOUND -> {
                            val errorMessage = stringResource(id = R.string.search_nothing_found)
                            ErrorText(errorMessage = errorMessage)
                        }
                        null -> Unit // Should never happen because isError already controls this
                    }
                }
            }
            AnimatedVisibility(
                visible = isTitlesAvailable,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val titleItems = uiState.value.titleItemsFull
                if (titleItems != null) {
                    TitleItemsList(
                        titleItemsFull = titleItems,
                        placeholderImage = placeholderImage,
                        onWatchlistClicked = { viewModel.onWatchlistClicked(it) },
                        onTitleClicked = { onTitleClicked(it) },
                        state = listState
                    )
                    LaunchedEffect(titleItems.isNotEmpty()) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                }
            }
        }
        SearchFAB(
            isFabVisible = listState.isScrollingUp(),
            onFabClicked = {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
        )
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SearchTextField(
    onSearchClicked: (String) -> Unit,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    var searchValue by remember { mutableStateOf("") }
    val showClearButton = searchValue.isNotEmpty()

    TextField(
        value = searchValue,
        onValueChange = { searchValue = it },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) keyboardController?.show()
            },
        label = { Text(text = stringResource(id = R.string.search_searchfield_label)) },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = MaterialTheme.shapes.medium,
        singleLine = true,
        trailingIcon = {
            AnimatedVisibility(
                visible = showClearButton,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = {
                    searchValue = ""
                    focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(id = R.string.cd_clear_field)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            keyboardController?.hide()
            onSearchClicked(searchValue)
            focusManager.clearFocus()
        })
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchFAB(
    isFabVisible: Boolean,
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isFabVisible,
        modifier = modifier,
        enter = scaleIn(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            transformOrigin = TransformOrigin(1f, 1f)
        ),
        exit = scaleOut(
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
            transformOrigin = TransformOrigin(1f, 1f)
        )
    ) {
        FloatingActionButton(onClick = onFabClicked) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.cd_discover_icon)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun SearchTextFieldPreview() {
    SearchTextField(
        onSearchClicked = {},
        focusManager = LocalFocusManager.current,
        keyboardController = null,
        focusRequester = FocusRequester()
    )
}

/**
 * Returns whether the lazy list is currently scrolling up.
 */
@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

