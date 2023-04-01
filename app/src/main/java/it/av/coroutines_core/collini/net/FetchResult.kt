package it.av.coroutines_core.collini.net

/**
 * Created by Antonio Vitiello on 13/03/2023.
 */
sealed class FetchResult<T> {

    class Success<T>(val data: T) : FetchResult<T>()

    class Error(val error: Throwable) : FetchResult<Nothing>()

    class Timeout : FetchResult<Nothing>()

}