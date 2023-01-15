package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.BACK_ID
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.SHARE_ID
import com.myapplications.mywatchlist.ui.details.CollapsingTopBarIds.TITLE_ID
import kotlin.math.roundToInt

private const val TAG = "COLLAPSING_TOOLBAR"

@Composable
fun DetailsCollapsingTopBar(
    fullHeight: Dp,
    scrollValue: () -> Int,
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {

    val collapseRange = with(LocalDensity.current) { (300.dp - 64.dp).toPx() }
    Log.d("TESTS", "collapseRange: $collapseRange ")
//    Log.d("TESTS", "scrollProvider: ${scrollProvider()} ")

    val collapseFractionProvider = {
        (scrollValue() / collapseRange).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .height(fullHeight)
//            .border(0.5.dp, Color.Red)
    ) {
        CollapsingTopBar(
            collapseFractionProvider = collapseFractionProvider,
            modifier = modifier
                .statusBarsPadding()
//                .height(fullHeight)
//                .border(0.5.dp, Color.Red)
        ) {
            Icon(
                modifier = Modifier
                    .wrapContentSize()
//                    .wrapContentHeight()
                    .layoutId(BACK_ID)
                    .clickable { onBackPressed() }
                    .padding(16.dp),
//                    .border(0.5.dp, Color.Yellow),
                imageVector = Icons.Filled.ArrowBack,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(id = com.myapplications.mywatchlist.R.string.cd_back_arrow)
            )
            /*Icon(
                modifier = Modifier
                    .wrapContentSize()
                    .layoutId(CollapsingTopBar.SHARE_ID)
                    .clickable { }
                    .padding(16.dp),
                imageVector = Icons.Filled.Share,
                tint = MaterialTheme.colors.onPrimary,
                contentDescription = stringResource(id = R.string.title_share)
            )*/
            Text(
                modifier = Modifier
                    .layoutId(TITLE_ID)
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp),
//                    .border(0.5.dp, Color.Blue),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CollapsingTopBar(
    modifier: Modifier = Modifier,
    collapseFractionProvider: () -> Float, // A value from (0-1) where 0 means fully expanded
    content: @Composable () -> Unit
) {
    val map = mutableMapOf<Placeable, Int>()
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val collapseFraction = collapseFractionProvider()
//        Log.d(TAG, "CollapsingTopBar: collapseFraction = $collapseFraction")
//        Log.d(TAG, "CollapsingTopBar: constraints = $constraints.. Layout direction: ${layoutDirection.name}")

        map.clear()
        val placeables = mutableListOf<Placeable>()
        measurables.map { measurable ->
            when (measurable.layoutId) {
                BACK_ID -> {
//                    Log.d(TAG, "CollapsingTopBar: BACK_ID measuring")
                    measurable.measure(constraints).also {

                    }
                }
                TITLE_ID -> {
                    val w = constraints.maxWidth - (collapseFraction * (placeables.first().width * 2)).toInt()
//                    Log.d(TAG, "CollapsingTopBar: TITLE_ID constraints width = $w")
                    measurable.measure(Constraints.fixedWidth(constraints.maxWidth - (collapseFraction * (placeables.first().width * 2)).toInt()))
                }

                else -> throw IllegalStateException("Id Not found")
            }.also { placeable ->
                map[placeable] = measurable.layoutId as Int
                placeables.add(placeable)
            }
        }

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                when (map[placeable]) {
                    BACK_ID -> {
                        placeable.placeRelative(0, 0)
                    }
                    TITLE_ID -> placeable.run {
                        val widthOffset = (placeables[0].width * collapseFraction).roundToInt()
                        val heightOffset = (placeables.first().height - placeable.height) / 2
//                        Log.d(TAG, "TITLE_ID: widthOffset= $widthOffset. Height offset = $heightOffset")
                        placeRelative(
                            widthOffset,
                            if (collapseFraction == 1f) heightOffset else constraints.maxHeight - height
                        )
                    }
                }
            }
        }
    }
}

object CollapsingTopBarIds {
    const val BACK_ID = 1001
    const val SHARE_ID = 1002
    const val TITLE_ID = 1003
    const val COLLAPSE_FACTOR = 0.6f
}
