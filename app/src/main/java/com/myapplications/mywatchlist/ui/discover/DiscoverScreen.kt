package com.myapplications.mywatchlist.ui.discover

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
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
import androidx.paging.compose.collectAsLazyPagingItems
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.extensions.isScrollingUp
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.ErrorText
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsListPaginated
import com.myapplications.mywatchlist.ui.entities.UiError
import com.myapplications.mywatchlist.ui.titlelist.*

private const val TAG = "SEARCH_SCREEN"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DiscoverScreen(
    placeHolderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    modifier: Modifier
) {
    val viewModel = hiltViewModel<DiscoverViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    // Remember a CoroutineScope to be able to scroll the list
    val coroutineScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val listState = rememberLazyListState()
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp)) {
            
            SearchTextField(
                viewModel = viewModel,
                onSearchClicked = viewModel::initializeSearch,
                focusManager = focusManager,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Crossfade(targetState = uiState) { discoverUiState ->
                when (discoverUiState) {
                    DiscoverUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LoadingCircle()
                        }
                    }
                    is DiscoverUiState.Error -> {
                        DiscoverListCenteredErrorMessage(
                            error = discoverUiState.error,
                            onButtonRetryClick = viewModel::retrySearch,
                        )
                    }
                    DiscoverUiState.FreshStart -> {
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
                    is DiscoverUiState.Ready -> {
                        val titles = discoverUiState.titles.collectAsLazyPagingItems()
                        TitleItemsListPaginated(
                            titles = titles,
                            error = {
                                viewModel.getErrorFromResultThrowable(it)
                            },
                            errorComposable = {
                                DiscoverListCenteredErrorMessage(
                                    error = it,
                                    onButtonRetryClick = { viewModel.retrySearch() }
                                )
                            },
                            onWatchlistClicked = {
                                viewModel.onWatchlistClicked(it)
                             },
                            onTitleClicked = { onTitleClicked(it) },
                            placeHolderPoster = placeHolderPoster,
                            listState = listState,
                            scope = coroutineScope
                        )
                    }
                }
            }
        }
        // TODO: Should remove FAB when Search Bar is implemented. Test the new SearchBar
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

@Composable
fun DiscoverListCenteredErrorMessage(
    error: UiError,
    onButtonRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (error) {
            DiscoverError.NO_INTERNET -> {
                ErrorText(
                    errorMessage =
                    stringResource(id = R.string.error_no_internet_connection),
                    onButtonRetryClick = onButtonRetryClick
                )
            }
            DiscoverError.FAILED_API_REQUEST,
            DiscoverError.UNKNOWN -> {
                ErrorText(
                    errorMessage =
                    stringResource(id = R.string.error_something_went_wrong),
                    onButtonRetryClick = onButtonRetryClick
                )
            }
            DiscoverError.NOTHING_FOUND -> {
                ErrorText(
                    errorMessage = stringResource(id = R.string.search_nothing_found)
                )
            }
            else -> ErrorText(
                errorMessage =
                stringResource(id = R.string.error_something_went_wrong),
                onButtonRetryClick = onButtonRetryClick
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SearchTextField(
    viewModel: DiscoverViewModel,
    onSearchClicked: () -> Unit,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    val searchValue by viewModel.searchString.collectAsState()
    val showClearButton = searchValue.isNotEmpty()

    TextField(
        value = searchValue,
        onValueChange = { viewModel.setSearchString(it) },
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
                    viewModel.setSearchString("")
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
            onSearchClicked()
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
        viewModel = hiltViewModel<DiscoverViewModel>(),
        onSearchClicked = {},
        focusManager = LocalFocusManager.current,
        keyboardController = null,
        focusRequester = FocusRequester()
    )
}



