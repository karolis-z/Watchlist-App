package com.myapplications.mywatchlist.ui.details

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.util.CurrencyFormatter
import com.myapplications.mywatchlist.domain.entities.*

private const val TAG = "EXTRA_DETAILS_SECTION"

@Composable
fun ExtraDetailsSection(
    title: Title,
    spokenLanguagesString: String?,
    modifier: Modifier = Modifier
) {
    val categoryStyle = MaterialTheme.typography.bodyLarge
    val infoStyle = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)

    val status = getStatusString(title = title)

    var expandedState by remember { mutableStateOf(false)}
    val rotationState by animateFloatAsState(targetValue = if (expandedState) 180f else 0f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeadline(
                label = stringResource(id = R.string.details_details_label),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { expandedState = !expandedState },
                modifier = Modifier.rotate(rotationState)
            ) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = stringResource(id = R.string.cd_expand_more)
                )
            }
        }

        // The details list
        AnimatedVisibility(
            visible = expandedState,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
        ) {
            Column(modifier = modifier.fillMaxWidth()) {

                //#region STATUS
                if (status.isNotEmpty()) {
                    Row(modifier = modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.details_status),
                            modifier = Modifier.weight(0.4f),
                            style = categoryStyle
                        )
                        Text(text = status, modifier = Modifier.weight(0.6f), style = infoStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                //#endregion

                //#region TAGLINE
                val tagline = title.tagline
                if (tagline != null) {
                    Row(modifier = modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.details_tagline),
                            modifier = Modifier.weight(0.4f),
                            style = categoryStyle
                        )
                        Text(text = tagline, modifier = Modifier.weight(0.6f), style = infoStyle)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                //#endregion

                //#region VOTE COUNT
                Row(modifier = modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.details_vote_count),
                        modifier = Modifier.weight(0.4f),
                        style = categoryStyle
                    )
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.details_votes,
                            count = title.voteCount.toInt(),
                            title.voteCount
                        ),
                        modifier = Modifier.weight(0.6f),
                        style = infoStyle
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                //#endregion

                //#region SPOKEN LANGUAGES
                if (spokenLanguagesString != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.details_spoken_languages),
                            modifier = Modifier.weight(0.4f),
                            style = categoryStyle
                        )
                        Text(
                            text = spokenLanguagesString,
                            modifier = Modifier.weight(0.6f),
                            style = infoStyle
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                //#endregion

                when (title) {
                    is Movie -> {
                        //#region REVENUE
                        val revenue = title.revenue
                        if (revenue != null) {
                            Row(modifier = modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.details_revenue),
                                    modifier = Modifier.weight(0.4f),
                                    style = categoryStyle
                                )
                                Text(
                                    text = CurrencyFormatter.getUsdAmountInLocalCurrencyFormat(revenue),
                                    modifier = Modifier.weight(0.6f),
                                    style = infoStyle
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        //#endregion

                        //#region BUDGET
                        val budget = title.budget
                        if (budget != null) {
                            Row(modifier = modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.details_budget),
                                    modifier = Modifier.weight(0.4f),
                                    style = categoryStyle
                                )
                                Text(
                                    text =
                                        CurrencyFormatter.getUsdAmountInLocalCurrencyFormat(budget),
                                    modifier = Modifier.weight(0.6f),
                                    style = infoStyle
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        //#endregion

                        //#region PROFIT
                        /* Both budget and revenue must be non-null and not equal to 0 to be able
                        to calculated a meaningful profit. If one parameter is 0, it means the data
                        is not available */
                        if (budget != null && revenue != null && budget != 0L && revenue != 0L) {
                            Row(modifier = modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = R.string.details_profit),
                                    modifier = Modifier.weight(0.4f),
                                    style = categoryStyle
                                )
                                Text(
                                    text = CurrencyFormatter.getUsdAmountInLocalCurrencyFormat(
                                        amount = revenue-budget
                                    ),
                                    modifier = Modifier.weight(0.6f),
                                    style = infoStyle
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        //#endregion

                        //#region IMDB
                        val imdbId = title.imdbId
                        if (imdbId != null) {
                            val context = LocalContext.current
                            Row(
                                modifier = modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.details_imdb_link),
                                    modifier = Modifier.weight(0.4f),
                                    style = categoryStyle
                                )
                                Box(
                                    modifier = Modifier
                                        .height(25.dp)
                                        .weight(0.6f)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.imdb_logo),
                                        contentDescription = stringResource(id = R.string.cd_imdb_link),
                                        contentScale = ContentScale.FillHeight,
                                        alignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(5.dp))
                                            .clickable { onImdbLinkClicked(context, imdbId) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        //#endregion
                    }
                    is TV -> {
                        //#region EPISODE COUNT
                        Row(modifier = modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(id = R.string.details_number_of_episodes),
                                modifier = Modifier.weight(0.4f),
                                style = categoryStyle
                            )
                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.details_episodes,
                                    count = title.numberOfEpisodes,
                                    title.numberOfEpisodes
                                ),
                                modifier = Modifier.weight(0.6f),
                                style = infoStyle
                            )
                        }
                        //#endregion
                    }
                }
            }
        }
    }
}

/**
 * Launches an IMDb link given then [imdbId]
 */
private fun onImdbLinkClicked(context: Context, imdbId: String) {
    val packageManager = context.packageManager
    val isImdbInstalled = try {
        val imdbIntent = packageManager.getLaunchIntentForPackage("com.imdb.mobile")
        imdbIntent != null
    } catch (e: Exception) {
        false
    }
    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    if (isImdbInstalled) {
        intent.data = android.net.Uri.parse("imdb:///title/$imdbId")
    } else {
        intent.data = android.net.Uri.parse(Constants.IMDB_BASE_URL + imdbId)
    }
    context.startActivity(intent)
}

@Composable
fun getStatusString(title: Title): String {
    return when(title) {
        is Movie -> {
            when(title.status) {
                MovieStatus.Released -> stringResource(id = R.string.title_status_released)
                MovieStatus.Rumored -> stringResource(id = R.string.title_status_rumored)
                MovieStatus.Planned -> stringResource(id = R.string.title_status_planned)
                MovieStatus.InProduction -> stringResource(id = R.string.title_status_in_production)
                MovieStatus.PostProduction -> stringResource(id = R.string.title_status_post_production)
                MovieStatus.Cancelled -> stringResource(id = R.string.title_status_cancelled)
                MovieStatus.Unknown -> stringResource(id = R.string.title_status_unknown)
            }
        }
        is TV -> {
            when(title.status) {
                TvStatus.Ended -> stringResource(id = R.string.title_status_ended)
                TvStatus.ReturningSeries -> stringResource(id = R.string.title_status_returning_series)
                TvStatus.Pilot -> stringResource(id = R.string.title_status_pilot)
                TvStatus.InProduction -> stringResource(id = R.string.title_status_in_production)
                TvStatus.Planned -> stringResource(id = R.string.title_status_planned)
                TvStatus.Cancelled -> stringResource(id = R.string.title_status_cancelled)
                TvStatus.Unknown -> stringResource(id = R.string.title_status_unknown)
            }
        }
        /* Defaulting to empty String. This could happen only if another type of Title gets
        added and this section is missed to be updated. Logging just in case. */
        else -> {
            Log.e(TAG, "getStatusString: Failed to find a string resource for Title's status")
            ""
        }
    }
}

@Preview
@Composable
fun ExtraDetailsSectionPreview() {

}
