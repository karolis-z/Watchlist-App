package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GenreChip(
    genreName: String,
    modifier: Modifier = Modifier,
    chipTonalElevation: Dp = 0.dp,
    borderWidth: Dp = 0.8.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textPaddingValues: PaddingValues = PaddingValues(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 7.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = chipTonalElevation,
        color = Color.Transparent,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = genreName,
                style = textStyle,
                // Different padding for top and bottom values because if equal, text looks a bit too low
                modifier = Modifier.padding(textPaddingValues),
                textAlign = TextAlign.Center
            )
        }
    }
}