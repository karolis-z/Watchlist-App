package com.myapplications.mywatchlist.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailsScreen(
    titleId: Int,
    titleType: String,
){
    Column(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        Text(text = "This is a title with id $titleId of type $titleType")

    }
}