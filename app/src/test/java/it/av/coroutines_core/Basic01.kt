package it.av.coroutines_core

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Created by Antonio Vitiello on 27/02/2023.
 * see:
 * https://kotlinlang.org/docs/coroutines-basics.html#coroutines-are-light-weight
 */
class Basic01 {
    companion object {
        const val TAG = "Basic01"
    }

    /**
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-basic-01.kt
     * Esegue una nuova coroutine e blocca il thread corrente fino al suo completamento.
     * Questa funzione è da utilizzare nelle funzioni principali e nei test, non deve essere utilizzata da una coroutine.
     * Il CoroutineDispatcher in questo caso è un'implementazione interna che elabora le continuazioni in questo thread
     * bloccato fino al completamento di questa coroutine.
     * Vedere CoroutineDispatcher per le altre implementazioni fornite da kotlinx.coroutines.
     * Quando CoroutineDispatcher viene specificato in modo esplicito nel contesto, la nuova coroutine viene eseguita
     * nel contesto del dispatcher specificato mentre il thread corrente è bloccato.
     * Se il dispatcher specificato è un ciclo di eventi di un altro runBlocking, questa chiamata utilizza il ciclo di
     * eventi esterno.
     * Se questo thread bloccato viene interrotto (vedere Thread.interrupt), il processo di coroutine viene annullato e
     * questa chiamata runBlocking genera InterruptedException.
     * Vedere newCoroutineContext per una descrizione delle funzionalità di debug disponibili per una coroutine appena
     * creata.
     * Il nome di runBlocking significa che il thread che lo esegue (in questo caso il thread principale) viene bloccato
     * per la durata della chiamata, fino a quando tutte le coroutine all'interno runBlocking{...} non completano la
     * loro esecuzione.
     * Un ambito esterno non può essere completato finché tutte le sue coroutine figli non sono state completate.
     */
    @Test
    fun `Esegue due routine contemporanemante`() = runBlocking {
        Log.d(TAG, ":Inizio!   ")
        this.launch(Dispatchers.Default) {
            for (i in 0..5) {
                delay(1000L) // blocking delay
                Log.d(TAG, "$i) Hello ") // main coroutine continues while a previous one is delayed
            }
        }
        this.launch(Dispatchers.Default) { // launch a new coroutine and continue
            for (i in 0..5) {
                delay(1000L) // non-blocking delay
                Log.d(TAG, "$i) World!") // print after delay
            }
        }
        Log.d(TAG, "Fine!") // main coroutine continues while a previous one is delayed
    }

    @Test
    fun `Invoke suspending fun and continue`() = runBlocking { // this: CoroutineScope
        launch(Dispatchers.IO) { repeatWorld() }
        for (i in 0..5) {
            Log.d(TAG, "$i) Hello ")
            delay(100L) // blocking delay
        }
        Log.d(TAG, "Fine!")
    }

    private suspend fun repeatWorld() {
        for (i in 0..5) {
            Log.d(TAG, "$i) World!") // print after delay
            delay(1000L) //blocking delay
        }
    }

    @Test
    fun `Invoke a suspending fun and wait`() = runBlocking {
        `suspending fun with multiple concurrent operations`()
        Log.d(TAG, "Fine!")
    }

    /**
     * executes both sections then return
     *
     * coroutineScope() è una funzione progettata per la scomposizione parallela del lavoro,
     * crea un CoroutineScope con un Job e chiama il blocco di sospensione specificato con questo scope.
     * NB: Quando una coroutine figlio in questo scope fallisce, tutto lo scope fallisce e gli altri figli
     * vengono cancellati!!
     *
     * supervisorScope() crea un CoroutineScope con un SupervisorJob e chiama il blocco di sospensione specificato
     * con questo scope.
     * NB: a differenza di coroutineScope, un errore di un elemento figlio non causa il fallimento di questo scope
     * e non influisce sugli altri elementi figlio, quindi è possibile implementare una politica personalizzata
     * per la gestione degli errori dei relativi elementi figlio!!
     */
    private suspend fun `suspending fun with multiple concurrent operations`() = coroutineScope {
        launch(Dispatchers.IO) {
            delay(2000L)
            Log.d(TAG, "World 2000")
        }
        launch(Dispatchers.IO) {
            delay(1000L)
            Log.d(TAG, "World 1000")
        }
        Log.d(TAG, "Hello")

//        Dispatchers.Main can't be used in Test
//        withContext(Dispatchers.Main) {
//            Log.d(TAG, "Hello")
//        }

    }

    @Test
    fun `Wait until a job completes`() = runBlocking {
        val job = launch(Dispatchers.Default) { // launch a new coroutine and keep a reference to its Job
            delay(1000L)
            Log.d(TAG, "World!")
        }
        Log.d(TAG, "Hello")
        job.join() // wait until child coroutine (Job) completes
        Log.d(TAG, "Done")
    }

    @Test
    fun `Repeat many times a coroutine without exhausts memory`() = runBlocking {
        var i = 0
        repeat(1000) { // launch a lot of coroutines
            launch(Dispatchers.IO) {
                delay(5000L)
                Log.d(TAG, ", ${++i}")
            }
        }
        Log.d(TAG, ", ${++i}")
    }

    /**
     * Concurrency:
     * https://kotlinlang.org/docs/coroutines-and-channels.html#concurrency
     * video:
     * https://www.youtube.com/watch?v=zEZc5AmHQhk
     */
    @Test
    fun `start coroutine with async`() = runBlocking {
        val deferred: Deferred<Int> = async(Dispatchers.IO) {
            loadData()
        }
        Log.d(TAG, "waiting...")
        val data: Int = deferred.await()
        Log.d(TAG, "loaded data is: $data")
    }

    private suspend fun loadData(): Int {
        Log.d(TAG, "loading...")
        delay(1000L)
        Log.d(TAG, "data loaded!")
        return 42
    }

    @Test
    fun `start more coroutines with async`() = runBlocking {
        val deferreds: List<Deferred<Int>> = (0..4).map { i ->
            async(Dispatchers.IO) { //tutte queste chiamate vengono fatte contemporaneamente in thread diversi
                loadData(i)
            }
        }
        Log.d(TAG, "waiting...")
        val data: List<Int> = deferreds.awaitAll()
        Log.d(TAG, "loaded data are: $data")
    }

    private suspend fun loadData(input: Int): Int {
        Log.d(TAG, "loading($input)...")
        delay(1000L)
        Log.d(TAG, "data loaded!")
        return 42 + input
    }

    @Test
    fun `cancelable coroutine`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (isActive) { // cancellable computation loop
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    Log.d(TAG, "job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        Log.d(TAG, "main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        Log.d(TAG, "main: Now I can quit.")
    }

    @Test
    fun `non cancelable coroutine`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) { // cancellable computation loop
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    Log.d(TAG, "job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        Log.d(TAG, "main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        Log.d(TAG, "main: Now I can quit.")
    }

    @Test
    fun `non cancelable coroutine not rethrows`() = runBlocking {
        val job = launch(Dispatchers.Default) {
            repeat(5) { i ->
                try {
                    // print a message twice a second
                    Log.d(TAG, "job: I'm sleeping $i ...")
                    delay(500)
                } catch (exc: CancellationException) {
                    Log.e(TAG, "ERROR: rethrows CancellationException.", exc)
                    //throw(exc)
                }
            }
        }
        delay(1300L) // delay a bit
        Log.d(TAG, "main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        Log.d(TAG, "main: Now I can quit.")
    }

    /**
     * https://kotlinlang.org/docs/cancellation-and-timeouts.html#timeout
     */
    @Test
    fun `timeout or null`() = runBlocking {
        val result = withTimeoutOrNull(2300L) {
            repeat(1000) { i ->
                Log.d(TAG, "I'm sleeping $i ...")
                delay(1000L)
            }
            "Done" // will get cancelled before it produces this result
        }
        Log.d(TAG, "Result is $result")
    }

    /**
     * Sequence in coroutines
     */
    @Test
    fun `invoca in sequenza due coroutine`() = runBlocking {
        val time = measureTimeMillis {
            val one = doSomethingUsefulOne()
            val two = doSomethingUsefulTwo()
            Log.d(TAG, "in sequenza: $one + $two = ${one + two}")
        }
        Log.d(TAG, "completato in: ${time}ms")
    }

    /**
     * Concurrent using async
     * concurrency with coroutines is always explicit!
     * https://kotlinlang.org/docs/composing-suspending-functions.html#concurrent-using-async
     */
    @Test
    fun `invoca due coroutine concorrenti`() = runBlocking {
        val time = measureTimeMillis {
            val oneDeferred = async { doSomethingUsefulOne() }
            val twoDeferred = async { doSomethingUsefulTwo() }
            val one = oneDeferred.await()
            val two = twoDeferred.await()
            Log.d(TAG, "coroutine concorrenti (Dispatchers.Default): $one + $two = ${one + two}")
        }
        Log.d(TAG, "completato in: ${time}ms")
    }

    /**
     * Parallel using async
     * usind thread pool of Dispatchers.IO
     */
    @Test
    fun `invoca due coroutine in parallelo`() = runBlocking {
        val time = measureTimeMillis {
            withContext(Dispatchers.IO) {
                val oneDeferred = async { doSomethingUsefulOne() }
                val twoDeferred = async { doSomethingUsefulTwo() }
                val one = oneDeferred.await()
                val two = twoDeferred.await()
                Log.d(TAG, "coroutine in parallelo (Dispatchers.IO): $one + $two = ${one + two}")
            }
        }
        Log.d(TAG, "completato in: ${time}ms")
    }

    /**
     * Parallel using async
     * concurrency with coroutines is always explicit!
     */
    @Test
    fun `invoca in parallelo due coroutine IO`() = runBlocking {
        val time = measureTimeMillis {
            withContext(Dispatchers.IO) {
                val one = async { doSomethingUsefulOne() }
                val two = async { doSomethingUsefulTwo() }
                Log.d(TAG, "in parallelo (IO): ${one.await()} + ${two.await()} = ${one.await() + two.await()}")
            }
        }
        Log.d(TAG, "completato in: ${time}ms")
    }

    /**
     * Concurrent using Lazily started async
     * https://kotlinlang.org/docs/composing-suspending-functions.html#lazily-started-async
     */
    @Test
    fun `invoca due coroutine concorrenti LAZY`() = runBlocking {
        val time = measureTimeMillis {
            val oneDeferred = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
            val twoDeferred = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }

            oneDeferred.start()
            twoDeferred.start()

            val one = oneDeferred.await()
            val two = twoDeferred.await()
            Log.d(TAG, "coroutine concorrenti LAZY: $one + $two = ${one + two}")
        }
        Log.d(TAG, "completato in: ${time}ms")
    }

    private suspend fun doSomethingUsefulOne(): Int {
        delay(1000L) // pretend we are doing something useful here
        Log.d(TAG, "doSomethingUsefulOne")
        return 13
    }

    private suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L) // pretend we are doing something useful here, too
        Log.d(TAG, "doSomethingUsefulTwo")
        return 29
    }

    /**
     * Structured concurrency with async: se una delle due coroutine fallisce in concurrentSum
     * concurrentSum lancia una eccezione e tutte le coroutine che sono state lanciate
     * nel suo ambito verranno cancellate!
     *
     * concurrency with coroutines is always explicit!
     * https://kotlinlang.org/docs/composing-suspending-functions.html#structured-concurrency-with-async
     */
    @Test
    fun `invoca due coroutine concorrenti strutturate`() = runBlocking {
        val time = measureTimeMillis {
            val sum = concurrentSum()
            Log.d(TAG, "coroutine concorrenti strutturate: ${sum}")
        }
        Log.d(TAG, "completato in: ${time}ms")
    }

    private suspend fun concurrentSum(): Int = coroutineScope {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        one.await() + two.await()
    }

}