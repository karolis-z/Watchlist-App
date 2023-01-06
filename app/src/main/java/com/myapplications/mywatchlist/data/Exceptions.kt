package com.myapplications.mywatchlist.data

sealed class SearchExceptions(message: String?, throwable: Throwable?) : Exception() {

    class NoConnectionException(message: String?, throwable: Throwable?) :
        SearchExceptions(message, throwable)

    class FailedApiRequestException(message: String?, throwable: Throwable?) :
        SearchExceptions(message, throwable)

    class NothingFoundException(message: String?, throwable: Throwable?) :
        SearchExceptions(message, throwable)

}

sealed class GetGenresExceptions(message: String?, throwable: Throwable?) : Exception() {

    class NoConnectionException(message: String?, throwable: Throwable?) :
        GetGenresExceptions(message, throwable)

    class FailedApiRequestException(message: String?, throwable: Throwable?) :
        GetGenresExceptions(message, throwable)

    class NothingFoundException(message: String?, throwable: Throwable?) :
        GetGenresExceptions(message, throwable)

}