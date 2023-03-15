package it.av.coroutines_core.collini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by Antonio Vitiello on 13/03/2023.
 */
class ColliniViewModelNotUsingViewModelScope(private val api: StackOverflowService) : ViewModel(), CoroutineScope {
    private val storage = FilesStorage()

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main


    /**
     * chiamate REST parallele
     * se fallisce una delle due rest badge o tag, l'altra continua
     * se fallisce topUser, viene cancellato il viewModelScope e quindi tutte le chiamate e killa l'app
     */
    suspend fun topUserWithRunCatch() {
        viewModelScope.launch {
            val user = api.fetchTopUsers()[0]

            val badgesDeferred = async {
                runCatching {
                    api.fetchBadges(user.id)
                }
            }
            val tagsDeferred = async {
                runCatching {
                    api.fetchTags(user.id)
                }
            }
            val badges = badgesDeferred.await().getOrNull()
            val tags = tagsDeferred.await().getOrNull()
            if (badges != null && tags != null) {
                updateUi(UserStats(user, badges, tags))
            } else {
                updateUi(UserStats(user, emptyList(), emptyList()))
                showErrorMessage()
            }
        }
    }

    private fun updateUi(data: UserStats) {
        // TODO
    }

    private fun showErrorMessage() {
        // TODO
    }

}