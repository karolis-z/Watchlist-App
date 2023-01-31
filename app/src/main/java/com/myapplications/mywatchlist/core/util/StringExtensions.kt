package com.myapplications.mywatchlist.core.util

import java.util.*

/**
 * Replacement for Kotlin's deprecated `capitalize()` function which capitalizes only the first
 * letter of a String irrespective of count of words in the String.
 */
fun String.capitalizedFirstLetter(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}