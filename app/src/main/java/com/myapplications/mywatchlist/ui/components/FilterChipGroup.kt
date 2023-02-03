package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
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
    var selected by remember { mutableStateOf(filters.indexOf(filter)) }

    Row(
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.defaultMinSize(minWidth = 30.dp)
    ) {
        for (index in filters.indices){
            val isSelected = index == selected
            FilterChip(
                label = { Text(text = getFilterLabel(titleFilter = filters[index])) },
                selected = isSelected,
                onClick = {
                    selected = index
                    onFilterSelected(filters[index])
                },
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = stringResource(
                                id = R.string.cd_selected_filter_chip,
                                getFilterLabel(titleFilter = filters[index])
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
fun getFilterLabel(titleFilter: TitleTypeFilter): String {
    return when(titleFilter){
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