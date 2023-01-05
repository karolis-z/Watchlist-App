package com.myapplications.mywatchlist.domain.result

sealed class BasicResult{
    data class Success(val message: String?): BasicResult()
    data class Failure(val exception: Throwable?): BasicResult()
}

