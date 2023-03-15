package it.av.coroutines_core.collini

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

/**
 * Created by Antonio Vitiello on 13/03/2023.
 */
class ColliniViewModelUsingCoroutineScope(private val api: StackOverflowService) : ViewModel() {
    private val scope = MainScope() // "coroutineContext" is: ContextScope(SupervisorJob() + Dispatchers.Main)
    private val storage = FilesStorage()


    /**
     * la stessa fun topUser ma con try-catch direttamente nell'assegnazione delle due variabili badges e tags
     */
    suspend fun topUser_2() {
        val user = api.fetchTopUsers()[0]

        val badgesDeferred = scope.async {
            try {
                api.fetchBadges(user.id)
            } catch (e: Exception) {
                null
            }
        }
        val tagsDeferred = scope.async {
            try {
                api.fetchTags(user.id)
            } catch (e: Exception) {
                null
            }
        }
        val (badges, tags) = badgesDeferred.await() to tagsDeferred.await()
        if (badges != null && tags != null) {
            updateUi(UserStats(user, badges, tags))
        } else {
            updateUi(UserStats(user, emptyList<Badge>(), emptyList<Tag>()))
            showErrorMessage()
        }
    }

    private fun updateUi(data: UserStats) {
        // TODO
    }

    private fun showErrorMessage() {
        // TODO
    }

}