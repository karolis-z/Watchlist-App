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
import java.time.LocalDate

private const val TAG = "EXTRA_DETAILS_SECTION"

@Composable
fun ExtraDetailsSection(
    title: Title,
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

                when (title) {
                    is Movie -> {
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
                    }
                    is TV -> {
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
    ExtraDetailsSection(
        title = getMovieForTesting(),

        )
}


//TODO: Remove once not needed anymore
private fun getMovieForTesting(): Movie {
    val genres = listOf(Genre(0, "Action"), Genre(1, "Science Fiction"), Genre(2, "Adventure"))
    val cast = listOf(
        CastMember(0, "Keanu Reaves", "Neo", null),
        CastMember(1, "Laurence Fishburne", "Morpheus", null),
        CastMember(2, "Carrie-Anne Moss", "Trinity", null),
        CastMember(3, "Hugo Weaving", "Agent Smith", null),
        CastMember(4, "Joe Pantoliano", "Cypher", null),
    )
    val videos = listOf(
        "https://www.youtube.com/watch?v=nUEQNVV3Gfs",
        "https://www.youtube.com/watch?v=RZ-MXBjvA38",
        "https://www.youtube.com/watch?v=L0fw0WzFaBM",
        "https://www.youtube.com/watch?v=m8e-FF8MsqU"
    )
    return Movie(
        id = 603,
        name = "The Matrix",
        imdbId = "tt0133093",
        overview = "Set in the 22nd century, The Matrix tells the story of a computer hacker who joins a group of underground insurgents fighting the vast and powerful computers who now rule the earth.",
        tagline = "Welcome to the Real World.",
        posterLink = "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
        backdropLink = "https://image.tmdb.org/t/p/w500/l4QHerTSbMI7qgvasqxP36pqjN6.jpg",
        genres = genres,
        cast = cast,
        videos = videos,
        status = MovieStatus.Released,
        releaseDate = LocalDate.parse("1999-03-30"),
        revenue = 463517383,
        runtime = 136,
        voteCount = 22622,
        voteAverage = 8.195,
        isWatchlisted = false
    )
}

//TODO: Remove once not needed anymore
private fun getTvForTesting(): TV {
    val genres = listOf(Genre(0, "Crime"), Genre(1, "Drama"))
    val cast = listOf(
        CastMember(
            0,
            "Giancarlo Esposito",
            "Leo Pap",
            "https://image.tmdb.org/t/p/w500/lBvDQZjxhIGMbH61iHnqerpbqHc.jpg"
        ),
        CastMember(
            1,
            "Paz Vega",
            "Ava Mercer",
            "https://image.tmdb.org/t/p/w500/fNLlJysFd5f0Q8Lj20EZpU8BiRN.jpg"
        ),
        CastMember(
            2,
            "Rufus Sewell",
            "Roger Salas",
            "https://image.tmdb.org/t/p/w500/yc2EWyg45GO03YqDttaEhjvegiE.jpg"
        ),
        CastMember(
            3,
            "Tati Gabrielle",
            "Hannah Kim",
            "https://image.tmdb.org/t/p/w500/zDtHNX7vXfhRmN2U5Ffmd9mLlo0.jpg"
        ),
        CastMember(
            4,
            "Peter Mark Kendall",
            "Stan Loomis",
            "https://image.tmdb.org/t/p/w500/9Cj5ySZ6znkNcASB5CZeibuDGsd.jpg"
        ),
    )
    val videos = listOf(
        "https://www.youtube.com/watch?v=T92iINbl0t4",
        "https://www.youtube.com/watch?v=YbArSoOP8XQ",
        "https://www.youtube.com/watch?v=nHGk3sRxjYM"
    )
    return TV(
        id = 156902,
        name = "Kaleidoscope",
        overview = "A master criminal and his crew hatch an elaborate scheme to break into a secure vault, but are forced to pivot when things don't go according to plan.",
        tagline = "There are 7 billion ways to solve a crime.",
        posterLink = "https://image.tmdb.org/t/p/w500/2nXJoSB5Y6R9ne7pjqL7Cs3zqY1.jpg",
        backdropLink = "https://image.tmdb.org/t/p/w500/kSqEenES71d1ApF2rRWxp5X0en5.jpg",
        genres = genres,
        cast = cast,
        videos = videos,
        status = TvStatus.Ended,
        releaseDate = LocalDate.parse("2023-01-01"),
        lastAirDate = LocalDate.parse("2023-01-01"),
        numberOfSeasons = 1,
        numberOfEpisodes = 9,
        voteCount = 97,
        voteAverage = 7.397,
        isWatchlisted = true
    )
}