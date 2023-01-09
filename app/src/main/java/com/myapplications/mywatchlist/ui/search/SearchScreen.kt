package com.myapplications.mywatchlist.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

private const val TAG = "SEARCH_SCREEN"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen() {
    val viewModel = hiltViewModel<SearchViewModel>()
    val uiState = viewModel.uiState.collectAsState()

    val placeholderImage = if (isSystemInDarkTheme()) {
        painterResource(id = R.drawable.placeholder_poster_dark)
    } else {
        painterResource(id = R.drawable.placeholder_poster_light)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)) {
        SearchTextField(
            onSearchClicked = { viewModel.searchTitleClicked(it) } ,
            focusManager = focusManager,
            keyboardController = keyboardController,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (!uiState.value.isSearchFinished && !uiState.value.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.search_not_searched_yet),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else if (uiState.value.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingCircle()
            }
        } else if (uiState.value.isNoData) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.search_nothing_found),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val titleItems = uiState.value.titleItems
            if (titleItems != null) {
                TitleItemsList(
                    titleItems = titleItems,
                    placeholderImage = placeholderImage,
                    onWatchlistClicked = { viewModel.onWatchlistClicked(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SearchTextField(
    onSearchClicked: (String) -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    var searchValue by remember { mutableStateOf("") }
    val showClearButton = searchValue.isNotEmpty()

    TextField(
        value = searchValue,
        onValueChange = { searchValue = it },
        modifier = modifier,
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

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun SearchTextFieldPreview() {
    SearchTextField(
        onSearchClicked = {},
        focusManager = LocalFocusManager.current,
        keyboardController = null,
    )
}


