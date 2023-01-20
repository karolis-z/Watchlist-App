package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.R

@Composable
fun ErrorText(
    errorMessage: String,
    modifier: Modifier = Modifier,
    errorTextStyle: TextStyle = MaterialTheme.typography.headlineMedium,
) {
    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage,
            style = errorTextStyle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorText(
    errorMessage: String,
    onButtonRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorTextStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    retryButtonTextStyle: TextStyle = MaterialTheme.typography.labelLarge
) {
    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage,
            style = errorTextStyle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onButtonRetryClick) {
            Text(
                text = stringResource(id = R.string.button_retry),
                style = retryButtonTextStyle,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}