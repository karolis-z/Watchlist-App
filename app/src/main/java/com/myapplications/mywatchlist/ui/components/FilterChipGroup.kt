package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipGroup(
    onFilterSelected: (TitleTypeFilter) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5.dp),
    filter: TitleTypeFilter = TitleTypeFilter.All
) {

    val filters = TitleTypeFilter.values().toList()

    Row(
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.defaultMinSize(minWidth = 30.dp)
    ) {
        filters.forEach { titleTypeFilter ->
            FilterChip(
                label = {
                    Text(
                        text = getFilterLabel(titleTypeFilter = titleTypeFilter),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                },
                selected = titleTypeFilter == filter,
                onClick = { onFilterSelected(titleTypeFilter) },
                leadingIcon = {
                    AnimatedVisibility(
                        visible = titleTypeFilter == filter,
                        enter = expandHorizontally(expandFrom = Alignment.End),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = stringResource(
                                id = R.string.cd_selected_filter_chip,
                                getFilterLabel(titleTypeFilter = titleTypeFilter)
                            ),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@Composable
fun getFilterLabel(titleTypeFilter: TitleTypeFilter): String {
    return when (titleTypeFilter) {
        TitleTypeFilter.All -> stringResource(id = R.string.filter_all)
        TitleTypeFilter.Movies -> stringResource(id = R.string.filter_movies)
        TitleTypeFilter.TV -> stringResource(id = R.string.filter_tv)
    }
}

enum class TitleTypeFilter {
    All,
    Movies,
    TV
}