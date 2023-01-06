package com.myapplications.mywatchlist.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isUnspecified

/**
 * @param minFontSizeCoefficient a value from 0.01 to 0.99. Indicates to which size the [text] shall
 * be reduced before using text overflow and ellipsizing [text]. E.g. a font size of 16.sp with 0.8
 * [minFontSizeCoefficient] will be reduced to 12.8.sp and if it still doesn't fit, it will be
 * ellipsized. Default value = 0.8.
 */
@Composable
fun AutoResizedText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    minFontSizeCoefficient: Double = 0.8,
    color: Color = textStyle.color
) {
    var resizedTextStyle by remember {
        mutableStateOf(textStyle)
    }
    var shouldDraw by remember {
        mutableStateOf(false)
    }

    val defaultFontSize = textStyle.fontSize

    // If text has been shrunk below minFontSizeCoefficient, then ellipsizing
    if (resizedTextStyle.fontSize < defaultFontSize * minFontSizeCoefficient) {
        Text(
            text = text,
            style = resizedTextStyle,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    } else {
        Text(
            text = text,
            color = color,
            modifier = modifier.drawWithContent {
                if (shouldDraw) {
                    drawContent()
                }
            },
            softWrap = false,
            style = resizedTextStyle,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (textStyle.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(
                            fontSize = defaultFontSize
                        )
                    }
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.95
                    )
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}