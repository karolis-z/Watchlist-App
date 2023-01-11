package com.myapplications.mywatchlist.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun DetailsScreen(
    titleId: Long,
    titleType: String,
){

    val viewModel = hiltViewModel<DetailsViewModel>()
    viewModel.getMovie(titleId)
    val uiState = viewModel.uiState.collectAsState()
    val movie = uiState.value

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(15.dp)) {
        Text(text = "This is a title with id $titleId of type $titleType")
        if (movie != null){
            Text(text = "Name : ${movie.name}")
            Text(text = "Genres : ${movie.genres}")
            AsyncImage(model = movie.posterLink, contentDescription = null, error = null)

        }
    }
}