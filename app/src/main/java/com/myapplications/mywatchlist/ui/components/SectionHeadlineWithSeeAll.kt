package com.myapplications.mywatchlist.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.R

@Composable
fun SectionHeadlineWithSeeAll(
    label: String,
    onSeeAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
    seeAllContentColor: Color = MaterialTheme.colorScheme.primary,
    bottomPadding: Dp = 5.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = bottomPadding)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(50))
                .clickable { onSeeAllClicked() }
                .padding(bottom = bottomPadding, top = bottomPadding, start = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.button_see_all),
                style = MaterialTheme.typography.bodyLarge,
                color = seeAllContentColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(id = R.string.cd_see_all),
                tint = seeAllContentColor
            )
        }
    }
}