package it.av.coroutines_core.mcquillan

/**
 * Created by Antonio Vitiello on 31/03/2023.
 */
sealed class Resource<T>(
    val data: T? = null,
    val errorMessage: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}