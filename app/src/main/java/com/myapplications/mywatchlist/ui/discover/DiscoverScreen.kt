package com.myapplications.mywatchlist.ui.discover

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.*
import com.myapplications.mywatchlist.ui.entities.UiError
import com.myapplications.mywatchlist.ui.titlelist.*

private const val TAG = "SEARCH_SCREEN"
private val SearchBarVerticalPadding: Dp = 8.dp
private val SearchBarHeight: Dp = 56.dp

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun DiscoverScreen(
    placeHolderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    systemUiController: SystemUiController = rememberSystemUiController()
) {
    val darkMode = isSystemInDarkTheme()
    LaunchedEffect(key1 = true) {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = !darkMode)
    }

    val viewModel = hiltViewModel<DiscoverViewModel>()
    val uiState by viewModel.searchViewState.collectAsState()
    val searchTitleFilter by viewModel.searchTitleType.collectAsState()

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val listState = rememberLazyListState()

    val searchValue by viewModel.searchString.collectAsState()
    var searchBarActive by rememberSaveable { mutableStateOf(false) }
    val showSearchResults by remember {
        derivedStateOf { !searchBarActive && searchValue.isNotBlank() }
    }

    val showClearButton by remember {
        derivedStateOf {
            searchValue.isNotBlank()
        }
    }

    fun closeSearchBar() {
        focusManager.clearFocus()
        searchBarActive = false
    }

    Box(
        modifier = modifier
            .padding(bottom = paddingValues.calculateBottomPadding())
            .fillMaxSize()
    ) {
        // Talkback focus order sorts based on x and y position before considering z-index. The
        // extra Box with fillMaxWidth is a workaround to get the search bar to focus before the
        // content.
        Box(
            Modifier
                .semantics { isContainer = true }
                .zIndex(1f)
                .fillMaxWidth()) {
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .focusRequester(focusRequester),
                query = searchValue,
                onQueryChange = { viewModel.setSearchString(it) },
                onSearch = {
                    closeSearchBar()
                    viewModel.onSearchClicked()
                },
                active = searchBarActive,
                onActiveChange = {
                    searchBarActive = it
                    if (!searchBarActive) focusManager.clearFocus()
                },
                placeholder = { Text(text = stringResource(id = R.string.search_searchfield_label)) },
                leadingIcon = {
                    Crossfade(targetState = searchBarActive) { active ->
                        when (active) {
                            true -> {
                                IconButton(onClick = {
                                    closeSearchBar()
                                    viewModel.clearSearch()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = stringResource(id = R.string.cd_back_arrow),
                                    )
                                }
                            }
                            false -> {
                                IconButton(onClick = {
                                    searchBarActive = true
                                    focusRequester.requestFocus()
                                }){
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(id = R.string.cd_search)
                                    )
                                }
                            }
                        }
                    }
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = showClearButton,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = {
                            viewModel.clearSearch()
                            if (!searchBarActive) {
                                searchBarActive = true
                                focusRequester.requestFocus()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.cd_clear_field)
                            )
                        }
                    }
                },
            ) {
                SearchViewContent(
                    uiState = uiState,
                    searchTitleFilter = searchTitleFilter,
                    onTitleTypeFilterSelected = { viewModel.setTitleTypeFilter(it) },
                    onRecentSearchClicked = {
                        viewModel.setSearchString(it)
                        closeSearchBar()
                    },
                    errorFromThrowable = { viewModel.getErrorFromResultThrowable(it) },
                    onWatchlistClicked = { viewModel.onWatchlistClicked(it) },
                    onTitleClicked = { onTitleClicked(it) },
                    placeHolderPoster = placeHolderPoster,
                    listState = listState
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding() + SearchBarHeight + SearchBarVerticalPadding,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Crossfade(targetState = showSearchResults) { showSearchResults ->
                when (showSearchResults) {
                    true -> {
                        SearchResultsContent(
                            uiState = uiState,
                            searchTitleFilter = searchTitleFilter,
                            onTitleTypeFilterSelected = { viewModel.setTitleTypeFilter(it) },
                            errorFromThrowable = { viewModel.getErrorFromResultThrowable(it) },
                            onWatchlistClicked = { viewModel.onWatchlistClicked(it) },
                            onTitleClicked = { onTitleClicked(it) },
                            placeHolderPoster = placeHolderPoster,
                            listState = rememberLazyListState()
                        )
                    }
                    false -> SearchScreenContent(
                        // TODO: Temporary implementation just to show some content
                        categoryTiles = listOf("Most Popular Comedies", "Most Popular Action Movies", "Most Popular Dramas", "Most Popular Comedies", "Most Popular Action Movies", "Most Popular Dramas", "Most Popular Comedies", "Most Popular Action Movies", "Most Popular Comedies", "Most Popular Action Movies", "Most Popular Comedies", "Most Popular Action Movies"),

                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsContent(
    uiState: SearchViewState,
    searchTitleFilter: TitleTypeFilter,
    onTitleTypeFilterSelected: (TitleTypeFilter) -> Unit,
    errorFromThrowable: (Throwable) -> UiError,
    onWatchlistClicked: (TitleItemFull) -> Unit,
    onTitleClicked: (TitleItemFull) -> Unit,
    placeHolderPoster: Painter,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    Column(modifier = modifier) {
        FilterChipGroup(onFilterSelected = onTitleTypeFilterSelected, filter = searchTitleFilter)
        Spacer(modifier = Modifier.height(5.dp))
        when (uiState) {
            is SearchViewState.ShowingRecent -> Unit
            // Only when state is Ready we can show the list
            is SearchViewState.Ready -> {
                val titles = uiState.titles.collectAsLazyPagingItems()
                TitleItemsListPaginated(
                    titles = titles,
                    error = errorFromThrowable,
                    errorComposable = {
                        DiscoverListCenteredErrorMessage(
                            error = it,
                            onButtonRetryClick = { titles.retry() }
                        )
                    },
                    onWatchlistClicked = onWatchlistClicked,
                    onTitleClicked = onTitleClicked,
                    placeHolderPoster = placeHolderPoster,
                    listState = listState
                )
            }
        }
    }
}

@Composable
fun SearchViewContent(
    uiState: SearchViewState,
    searchTitleFilter: TitleTypeFilter,
    onTitleTypeFilterSelected: (TitleTypeFilter) -> Unit,
    onRecentSearchClicked: (String) -> Unit,
    errorFromThrowable: (Throwable) -> UiError,
    onWatchlistClicked: (TitleItemFull) -> Unit,
    onTitleClicked: (TitleItemFull) -> Unit,
    placeHolderPoster: Painter,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    Column(modifier = modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp)) {
        FilterChipGroup(onFilterSelected = onTitleTypeFilterSelected, filter = searchTitleFilter)
        Spacer(modifier = Modifier.height(5.dp))
        Crossfade(targetState = uiState) { searchViewState ->
            when (searchViewState) {
                is SearchViewState.ShowingRecent -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(searchViewState.recentSearched) { recentSearch: String ->
                            ListItem(
                                headlineText = {
                                    Text(
                                        text = recentSearch,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                },
                                leadingContent = {
                                    Icon(Icons.Filled.History, contentDescription = null)
                                },
                                modifier = Modifier.clickable { onRecentSearchClicked(recentSearch) }
                            )
                        }
                    }
                }
                is SearchViewState.Ready -> {
                    val titles = searchViewState.titles.collectAsLazyPagingItems()
                    TitleItemsListPaginated(
                        titles = titles,
                        error = errorFromThrowable,
                        errorComposable = {
                            DiscoverListCenteredErrorMessage(
                                error = it,
                                onButtonRetryClick = { titles.retry() }
                            )
                        },
                        onWatchlistClicked = onWatchlistClicked,
                        onTitleClicked = onTitleClicked,
                        placeHolderPoster = placeHolderPoster,
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
fun SearchScreenContent(
    // TODO: Should later introduce a Tile object here and get info which Tiles to
    //  display from the data layer
    categoryTiles: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /*TODO*/ },
            shape = RoundedCornerShape(8.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.search_button_custom_filter),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(id = R.string.search_label_browse_categories),
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(categoryTiles){ categoryLabel ->
                CategoryTileCard(label = categoryLabel)
            }
        }
    }
}

@Composable
fun CategoryTileCard(
    label: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.height(100.dp)) {
        Text(
            text = label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp)
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
            SearchViewError.NO_INTERNET -> {
                ErrorText(
                    errorMessage =
                    stringResource(id = R.string.error_no_internet_connection),
                    onButtonRetryClick = onButtonRetryClick
                )
            }
            SearchViewError.FAILED_API_REQUEST,
            SearchViewError.UNKNOWN -> {
                ErrorText(
                    errorMessage =
                    stringResource(id = R.string.error_something_went_wrong),
                    onButtonRetryClick = onButtonRetryClick
                )
            }
            SearchViewError.NOTHING_FOUND -> {
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
