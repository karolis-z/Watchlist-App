package com.myapplications.mywatchlist.ui.details.management.states.toolbar

abstract class DynamicOffsetScrollFlagState(
    heightRange: IntRange,
    scrollValue: Int
) : ScrollFlagState(heightRange, scrollValue) {

    protected abstract var scrollOffset: Float

}