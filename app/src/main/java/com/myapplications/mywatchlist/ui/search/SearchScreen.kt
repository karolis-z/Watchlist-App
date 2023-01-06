package com.myapplications.mywatchlist.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.ui.components.AutoResizedText
import com.myapplications.mywatchlist.ui.components.GenreChip
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val viewModel = hiltViewModel<SearchViewModel>()
    val uiState = viewModel.uiState.collectAsState()

    var searchValue by remember { mutableStateOf("") }

    Column() {
        OutlinedTextField(
            value = searchValue,
            onValueChange = {
                searchValue = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
        )
        Button(
            onClick = { viewModel.searchTitleClicked(searchValue) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Search")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items = uiState.value) { item: TitleItem ->
                TitleItemCard(
                    title = item,
                    onWatchlistClicked = { viewModel.onWatchlistClicked(item) }
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleItemCard(
    title: TitleItem,
    onWatchlistClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
//                .border(1.dp, Color.Green)
        )
        {
            // Column with poster
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
//                    .border(1.dp, Color.Red)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.placeholder_poster),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Column with all Title info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp, horizontal = 10.dp)
            )
            {
                Row() {
                    AutoResizedText(
                        text = title.name,
                        textStyle = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(key = { genre -> genre.id }, items = title.genres) { genre: Genre ->
                        GenreChip(genreName = genre.name)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TitleItemCardPreview() {
    TitleItemCard(
        title = TitleItem(
            id = 0,
            name = "Matrix Resurrections",
            type = TitleType.MOVIE,
            mediaId = 0,
            overview = "Plagued by strange memories, Neo's life takes an unexpected turn when he finds himself back inside the Matrix",
            posterLink = null,
            genres = listOf(Genre(0, "Action"), Genre(1, "Science Fiction"), Genre(2, "Adventure")),
            releaseDate = LocalDate.now(),
            voteCount = 10000,
            voteAverage = 8.8
        ),
        onWatchlistClicked = {},
    )
}

// TODO: Basic functionality to just test showing the list
@Composable
fun TitleItemCardTest(
    title: TitleItem,
    onWatchlistClicked: () -> Unit,
    onUnWatchlistClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 10.dp)
    ) {
        Text(text = title.name)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title.overview ?: "", maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title.voteAverage.toString())
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            Button(onClick = onWatchlistClicked) {
                Text(text = "Add to Watchlist")
            }
            Button(onClick = onUnWatchlistClicked) {
                Text(text = "UnWatchlist")
            }
        }
    }
}