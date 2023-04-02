package it.av.coroutines_core.collini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.av.coroutines_core.collini.FilesStorage
import it.av.coroutines_core.collini.net.FetchResult
import it.av.coroutines_core.collini.net.StackOverflowService
import it.av.coroutines_core.collini.net.model.Repo
import it.av.coroutines_core.collini.net.model.User
import it.av.coroutines_core.collini.net.model.UserStats
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

/**
 * Created by Antonio Vitiello on 13/03/2023.
 * see:
 * https://proandroiddev.com/async-code-using-kotlin-coroutines-233d201099ff
 * https://proandroiddev.com/managing-exceptions-in-nested-coroutine-scopes-9f23fd85e61
 * https://www.youtube.com/watch?v=Cq3di5lfMkY&t=1203s
 */
class ColliniViewModel(private val api: StackOverflowService) : ViewModel() {
    private val storage = FilesStorage()
    private val fileAccessScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    //Prefetch: questa REST call parte immediatamente dopo l'istanziazione della classe
    private val topRepoDeferred = viewModelScope.async {
        val result = runCatching { api.fetchTopRepo() }
        result.getOrNull()
    }


    fun loadTopUsers_NOT_OPTIMIZED() {                      //Thread: main
        viewModelScope.launch {
            val dataFromCache = storage.loadFromFile()      //Thread: IO
            val data = if (dataFromCache != null) {         //Thread: main
                dataFromCache                               //Thread: main
            } else {                                        //Thread: main
                val fetchedData = api.fetchTopUsers()[0]    //Thread: IO
                storage.saverUser(fetchedData)             //Thread: IO
                fetchedData                                 //Thread: main
            }                                               //Thread: main
            updateUi(data)                                  //Thread: main
        }                                                   //Thread: main
    }

    suspend fun loadTopUsers() {                                //Thread: main
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                val dataFromCache = storage.loadFromFile()      //Thread: IO
                if (dataFromCache != null) {                    //Thread: IO
                    dataFromCache                               //Thread: IO
                } else {                                        //Thread: IO
                    val fetchedData = api.fetchTopUsers()[0]    //Thread: IO
                    storage.saverUser(fetchedData)             //Thread: IO
                    fetchedData                                 //Thread: IO
                }                                               //Thread: IO
            }                                                   //Thread: IO
            updateUi(data)                                      //Thread: main
        }                                                       //Thread: main
    }

    /**
     * Fire & Forget:
     * lo stesso di "loadTopUsers" ma il saverUser viene eseguito in background grazie allo scope "fileAccessScope"
     * quindi la funzione ritorna prima che il salvataggio su file system sia avvenuto!
     */
    suspend fun loadTopUsersWithFireAndForget() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                val dataFromCache = storage.loadFromFile()
                if (dataFromCache != null) {
                    dataFromCache
                } else {
                    val topUserData = api.fetchTopUsers()[0]
                    fileAccessScope.launch {
                        try {
                            storage.saverUser(topUserData)
                        } catch (e: Exception) {
                            showErrorMessage()
                        }
                    }
                    topUserData
                }
            }
            updateUi(data)
        }
    }

    /**
     * chiamate REST insequenza (concorrenti non paralleli)
     * se una delle rest fallisce falliscono tutte e tre le chiamate e killa l'app
     */
    suspend fun topUser_NO_PARALLEL(): UserStats {
        val user = api.fetchTopUsers()[0]
        val badges = api.fetchBadges(user.id)
        val tags = api.fetchTags(user.id)
        return UserStats(user, badges, tags)
    }

    /**
     * chiamate REST parallele
     * se fallisce una delle due rest badge o tag, viene cancellato il coroutineScope ma non la topUser
     * se fallisce topUser, viene cancellato il viewModelScope e quindi tutte le chiamate e killa l'app
     */
    suspend fun topUser() {
        viewModelScope.launch {
            val user = api.fetchTopUsers()[0]

            try {
                val (badges, tags) = coroutineScope {
                    val badgesDeferred = async { api.fetchBadges(user.id) }
                    val tagsDeferred = async { api.fetchTags(user.id) }
                    badgesDeferred.await() to tagsDeferred.await()
                }
                updateUi(UserStats(user, badges, tags))
            } catch (e: Exception) {
                updateUi(UserStats(user, emptyList(), emptyList()))
                showErrorMessage()
            }
        }
    }

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

    /**
     * la call REST è già stata invocata alla creazione della classe: Prefetched
     */
    fun loadTopRepo() {
        viewModelScope.launch {
            try {
                updateUi(topRepoDeferred.await() ?: emptyList())
            } catch (e: Exception) {
                showErrorMessage()
            }
        }
    }

    /**
     * Timeout su call REST: 2 sec.
     */
    fun topUsersWithTimeout() {
        try {
            viewModelScope.launch {
                withTimeout(2000L) {
                    val topRepos = api.fetchTopRepo()
                    updateUi(topRepos)
                }
            }
        } catch (e: TimeoutCancellationException) {
            //TODO: manage Timeout
            showErrorMessage()
        } catch (e: Exception) {
            //TODO: manage network error
            showErrorMessage()
        }
    }

    suspend fun getTopUserData(): UserStats {
        val user = api.fetchTopUsers()[0]
        return try {
            val (badges, tags) = coroutineScope {
                val badgesDeferred = async { api.fetchBadges(user.id) }
                val tagsDeferred = async { api.fetchTags(user.id) }
                badgesDeferred.await() to tagsDeferred.await()
            }
            UserStats(user, badges, tags)
        } catch (e: Exception) {
            showErrorMessage()
            UserStats(user, emptyList(), emptyList())
        }
    }

    /**
     * SELECT:
     * eseguo le chiamate fetchBadges e fetchTags (in getTopUserData) e setto un timeout di 2 sec.
     * se torna prima la REST cancello il timeout ed aggiorno la UI, in caso errore carica i dati dalla cache su File
     * esguo due chiamate ma già al termine della prima eseguo un'altra azione
     * see:
     * https://youtu.be/Cq3di5lfMkY?t=2814
     */
    fun loadDataWithSelect() {
        viewModelScope.launch(Dispatchers.IO) {

            //preparo la fetch per la select
            val fetch = viewModelScope.async {
                try {
                    FetchResult.Success(getTopUserData())
                } catch (e: Exception) {
                    FetchResult.Error(e)
                }
            }

            //preparo il timeout per la select
            val timeout = async {
                delay(2000L)
                FetchResult.Timeout()
            }

            //SELECT: result ritorna il primo dei due eventi che si verifica: la REST oppure il Timeout
            val result = select<FetchResult<out UserStats>> {
                fetch.onAwait { it }
                timeout.onAwait { it }
            }

            //analizzo il risultato della select
            when (result) {
                is FetchResult.Success -> {
                    timeout.cancel()
                    updateUi(result.data)
                }
                is FetchResult.Timeout -> {
                    storage.loadFromFile()?.let {
                        updateUi(it)
                    }
                    val fetchResult = fetch.await()
                    (fetchResult as? FetchResult.Success)?.let { success: FetchResult.Success<out UserStats> ->
                        updateUi(success.data)
                    }
                }
                is FetchResult.Error -> {
                    timeout.cancel()
                    storage.loadFromFile()?.let {
                        updateUi(it)
                    } ?: run {
                        showErrorMessage()
                    }
                }
            }
        }
    }

    private fun updateUi(data: UserStats) {
        // TODO
    }

    private fun showErrorMessage() {
        // TODO
    }

    private fun updateUi(data: User) {
        // TODO
    }

    private fun updateUi(repos: List<Repo>) {
        // TODO
    }

    /**
     * è lecito scrivere un ciclo infinito in una coroutine in un viewModelScope senza creare leaks
     * poiché viewModelScope eseguirà automaticamente cancel sulle sue coroutine quando il ViewModel cessa
     */
    fun runForever() {
        // start a new coroutine in the ViewModel
        viewModelScope.launch {
            // cancelled when the ViewModel is cleared
            while (true) {
                delay(1_000)
                // do something every second
            }
        }
    }

    /**
     * questa funzione viene invocata quando questo ViewModel sta per essere distrutto
     * qui ad esempio viene eseguito il viewModelScope.cancel() per evitare memory leak o job leak
     * see: https://medium.com/androiddevelopers/coroutines-on-android-part-ii-getting-started-3bff117176dd
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}