package com.myapplications.mywatchlist.data

sealed class ApiGetTitleItemsExceptions(message: String?, throwable: Throwable?) : Exception() {

    class NoConnectionException(message: String?, throwable: Throwable?) :
        ApiGetTitleItemsExceptions(message, throwable)

    class FailedApiRequestException(message: String?, throwable: Throwable?) :
        ApiGetTitleItemsExceptions(message, throwable)

    class NothingFoundException(message: String?, throwable: Throwable?) :
        ApiGetTitleItemsExceptions(message, throwable)

}

sealed class ApiGetGenresExceptions(message: String?, throwable: Throwable?) : Exception() {

    class NoConnectionException(message: String?, throwable: Throwable?) :
        ApiGetGenresExceptions(message, throwable)

    class FailedApiRequestException(message: String?, throwable: Throwable?) :
        ApiGetGenresExceptions(message, throwable)

    class NothingFoundException(message: String?, throwable: Throwable?) :
        ApiGetGenresExceptions(message, throwable)

}

sealed class ApiGetDetailsException(message: String?, throwable: Throwable?) : Exception() {

    class NoConnectionException(message: String?, throwable: Throwable?) :
        ApiGetDetailsException(message, throwable)

    class FailedApiRequestException(message: String?, throwable: Throwable?) :
        ApiGetDetailsException(message, throwable)

    class NothingFoundException(message: String?, throwable: Throwable?) :
        ApiGetDetailsException(message, throwable)

}