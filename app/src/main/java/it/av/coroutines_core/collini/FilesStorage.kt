package it.av.coroutines_core.collini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream

/**
 * Created by Antonio Vitiello on 13/03/2023.
 */
class FilesStorage {

    suspend fun loadFromFile(): User? = withContext(Dispatchers.IO) {
        val result = runCatching {
            FileInputStream(getCacheFile())
                .bufferedReader()
                .use { reader: BufferedReader ->
                    reader.readText()
                }
        }
        return@withContext parseData(result.getOrNull())
    }

    private fun getCacheFile(): File {
        return File("users-cache")
    }

    private fun parseData(name: String?): User? {
        return name?.let { User(it) }
    }

    suspend fun saverUser(topUserData: User) = withContext(Dispatchers.IO) {
        // TODO
    }
}