package com.myapplications.mywatchlist.ui.details.toolbarstate

import androidx.compose.runtime.*

@Stable
interface ToolbarState {
    val offset: Float
    val height: Float
    val progress: Float
    var scrollValue: Int
}

abstract class ScrollFlagState(heightRange: IntRange, scrollValue: Int) : ToolbarState {

    init {
        require(heightRange.first >= 0 && heightRange.last >= heightRange.first) {
            "The lowest height value must be >= 0 and the highest height value " +
                    "must be >= the lowest value."
        }
    }

    protected val minHeight = heightRange.first
    protected val maxHeight = heightRange.last
    protected val rangeDifference = maxHeight - minHeight

    protected var _scrollValue by mutableStateOf(
        value = scrollValue.coerceAtLeast(0),
        policy = structuralEqualityPolicy()
    )

    final override val progress: Float
        get() = 1 - (maxHeight - height) / rangeDifference

}