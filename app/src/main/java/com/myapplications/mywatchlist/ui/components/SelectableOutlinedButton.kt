package com.myapplications.mywatchlist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun SelectableOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    unSelectedContainerColor: Color = Color.Transparent,
    unSelectedContentColor: Color = MaterialTheme.colorScheme.primary,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) selectedContainerColor else unSelectedContainerColor,
        animationSpec = tween(durationMillis = 250)
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unSelectedContentColor,
        animationSpec = tween(durationMillis = 250)
    )
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}