package com.myapplications.mywatchlist.data

sealed class SearchException(message: String?, throwable: Throwable?) : Exception() {

    class NoConnectionException(message: String?, throwable: Throwable?) :
        SearchException(message, throwable)

    class FailedApiRequestException(message: String?, throwable: Throwable?) :
        SearchException(message, throwable)

    class NothingFoundException(message: String?, throwable: Throwable?) :
        SearchException(message, throwable)

}