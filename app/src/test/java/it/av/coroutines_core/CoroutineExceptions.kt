package it.av.coroutines_core

import kotlinx.coroutines.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Created by Antonio Vitiello on 10/03/2023.
 * see:
 * https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/topics/exception-handling.md
 */
class CoroutineExceptions {
    companion object {
        const val TAG = "CoroutineExceptions"
    }

    @Test
    @OptIn(DelicateCoroutinesApi::class)
    fun `GlobalScope exception handling`() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler got $exception")
        }
        val job = GlobalScope.launch(handler) { // root coroutine, running in GlobalScope
            throw AssertionError()
        }
        val deferred = GlobalScope.async(handler) { // also root, but async instead of launch
            throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
        }
        joinAll(job, deferred)
    }


    @Test
    fun `SupervisorJob exception handling`() = runBlocking {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler got $exception")
        }

        val supervisor = SupervisorJob()
        with(CoroutineScope(coroutineContext + supervisor)) {
            // launch the first child -- its exception is ignored for this example (don't do this in practice!)
            val firstChild = launch(coroutineExceptionHandler) {
                Log.d(TAG, "The first child is failing")
                throw AssertionError("The first child is cancelled")
            }
            // launch the second child
            val secondChild = launch {
                firstChild.join()
                // Cancellation of the first child is not propagated to the second child
                Log.d(TAG, "Is the first child cancelled? ${firstChild.isCancelled}; the second is active")
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    // But cancellation of the supervisor is propagated
                    Log.d(TAG, "The second child is cancelled because the supervisor was cancelled")
                }
            }
            // wait until the first child fails & completes
            firstChild.join()
            Log.d(TAG, "Cancelling the supervisor")
            supervisor.cancel()
            secondChild.join()
        }
    }


    @Test
    fun `Supervisor scope exception handling`() = runBlocking {
        try {
            supervisorScope {
                val child = launch {
                    try {
                        Log.d(TAG, "child: going in a long delay")
                        delay(Long.MAX_VALUE)
                    } finally {
                        Log.d(TAG, "child: finally ends, has been cancelled")
                    }
                }
                // Give our child a chance to execute and print error using yield
                yield()
                Log.d(TAG, "Throwing an exception from the supervisorScope")
                throw AssertionError()
            }
        } catch (e: AssertionError) {
            Log.e(TAG, "Caught an assertion error", e)
        }
    }


    @Test
    fun `Exception in supervisorScope`() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler catched: $exception")
        }
        supervisorScope {
            Log.d(TAG, "The scope is started")

            launch(handler) {
                Log.d(TAG, "'launch' in scope is throwing an exception")
                throw ArithmeticException()
            }

            Log.d(TAG, "The scope is completing")
        }
        Log.d(TAG, "The scope is completed")
    }


    /**
     * CoroutineExceptionHandler è sempre installato in una coroutine creata in GlobalScope .
     * Non ha senso installare un gestore di eccezioni in una coroutine avviata nell'ambito del runBlocking
     * principale, poiché la coroutine principale verrà sempre annullata quando il relativo figlio viene
     * completato con un'eccezione nonostante il gestore installato
     */
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `Throwing Exception in GlobalScope`() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler catched: $exception")
        }
        val job = GlobalScope.launch(handler) { // root coroutine, running in GlobalScope
            Log.d(TAG, "'launch' in GlobalScope is throwing an exception")
            throw NumberFormatException()
        }
        val deferred = GlobalScope.async(handler) { // also root, but async instead of launch
            delay(500)
            Log.d(TAG, "'async' in GlobalScope is throwing an exception")
            throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
        }
        joinAll(job, deferred)
        Log.d(TAG, "The scope is completed")
    }

    @Test
    fun `Throwing Exception without scope`() = runBlocking {
        launch {
            try {
                Log.d(TAG, "'launch' in CoroutineScope is throwing an exception")
                throw NumberFormatException()
            } catch (exc: Exception) {
                Log.d(TAG, "Exception cought in 'launch': $exc")
            }
        }

        async {
            try {
                Log.d(TAG, "'async' in CoroutineScope is throwing an exception")
                throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
            } catch (exc: Exception) {
                Log.d(TAG, "Exception cought in 'async': $exc")
            }
        }.await()

        Log.d(TAG, "The scope is completed")
    }

    @Test
    fun `Throwing Exception in coroutineScope`() = runBlocking {
        try {
            coroutineScope {
                launch { // root coroutine, running in GlobalScope
                    delay(500)
                    Log.d(TAG, "'launch' in coroutineScope is throwing an exception")
                    throw NumberFormatException()
                }

                async { // also root, but async instead of launch
                    Log.d(TAG, "'async' in coroutineScope is throwing an exception")
                    throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
                }.await()
            }
        } catch (exc: Exception) {
            Log.d(TAG, "Exception cought in 'coroutineScope': $exc")
        }

        Log.d(TAG, "The scope is completed")
    }


    @Test
    fun `Cancelling SupervisorJob`() = runBlocking {
        val supervisor = SupervisorJob()
        with(CoroutineScope(coroutineContext + supervisor)) {
            // launch the first child -- its exception is ignored for this example (don't do this in practice!)
            val firstChild = launch(CoroutineExceptionHandler { _, _ -> }) {
                println("The first child is failing")
                throw AssertionError("The first child is cancelled")
            }
            // launch the second child
            val secondChild = launch {
                firstChild.join()
                // Cancellation of the first child is not propagated to the second child
                println("The first child is cancelled: ${firstChild.isCancelled}, but the second one is still active")
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    // But cancellation of the supervisor is propagated
                    println("The second child is cancelled because the supervisor was cancelled")
                }
            }
            // wait until the first child fails & completes
            firstChild.join()
            println("Cancelling the supervisor")
            supervisor.cancel()
            secondChild.join()
        }
    }


    /**
     * If an async fails, cancel coroutineScope and all coroutines inside
     * but UI receive error message
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `coroutineScope cancel all coroutines on Exception`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        try {
            val (one, two) = coroutineScope {
                val deferredOne = async(testDispatcher) { doSomethingUsefulOne() }
                val deferredTwo = async(testDispatcher) { doSomethingUsefulTwo(success = false) }
                deferredOne.await() to deferredTwo.await()
            }
            updateUI(one, two)
        } catch (exc: Exception) {
            Log.e(TAG, "ERROR LOG!", exc)
            showErrorUI("Error occurred: ${exc.message}")
        }
    }

    /**
     * If an async fails, cancel scope and return empty result to UI
     * with Log of error message
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `if a coroutine fails get empty results`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val (one, two) = try {
            coroutineScope {
                val deferredOne = async(testDispatcher) { doSomethingUsefulOne() }
                val deferredTwo = async(testDispatcher) { doSomethingUsefulTwo(success = false) }
                deferredOne.await() to deferredTwo.await()
            }
        } catch (exc: Exception) {
            Log.e(TAG, "Error occurred in a doSomethingUseful", exc)
            -1 to -1
        }

        updateUI(one, two)
    }

    /**
     * If an async fails, continue and get the result of other coroutine
     * with Log of error message
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `if coroutineScope fails get result of second coroutines`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)

        val resultOne = async(testDispatcher) {
            runCatching {
                doSomethingUsefulOne(success = false)
            }
        }.await()

        val resultTwo = async(testDispatcher) {
            runCatching {
                doSomethingUsefulTwo()
            }
        }.await()

        if (resultOne.isFailure) {
            Log.e(TAG, "Error occurred in doSomethingUsefulOne", resultOne.exceptionOrNull())
        }
        if (resultTwo.isFailure) {
            Log.e(TAG, "Error occurred in doSomethingUsefulTwo", resultTwo.exceptionOrNull())
        }

        updateUI(resultOne.getOrNull(), resultTwo.getOrNull())
    }

    private suspend fun doSomethingUsefulOne(success: Boolean = true): Int {
        delay(1000L) // pretend we are doing something useful here
        Log.d(TAG, "executing: doSomethingUsefulOne")
        return if (success) {
            111
        } else {
            throw ArithmeticException("doSomethingUsefulOne: error emulated for test!")
        }
    }

    private suspend fun doSomethingUsefulTwo(success: Boolean = true): Int {
        delay(1000L) // pretend we are doing something useful here, too
        Log.d(TAG, "executing: doSomethingUsefulTwo")
        return if (success) {
            222
        } else {
            throw IndexOutOfBoundsException("doSomethingUsefulTwo: error emulated for test!")
        }
    }

    private fun updateUI(one: Int?, two: Int?) {
        Log.d(TAG, "Update UI with [$one, $two]")
    }

    private fun showErrorUI(msg: String) {
        Log.d(TAG, "SHOW Error on UI: $msg")
    }


    /**
     * Utilizzare SupervisorJob o supervisorScope
     * quando NON si desidera che un errore annulli il genitore e i fratelli
     * quindi se child#1 fallisce, né child#2 né lo scope verrà annullato.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `dont want a failure cancel parent or siblings`() = runTest {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler catched: $exception")
        }
        supervisorScope {
            Log.d(TAG, "launch doSomethingUsefulOne")
            val job1 = launch(handler) {
                delay(500)
                doSomethingUsefulOne(success = false)
                Log.d(TAG, "will not be executed: doSomething after a fail in this scope!!!")
                doSomethingUsefulOne()
            }
            Log.d(TAG, "launch doSomethingUsefulTwo")
            val job2 = launch(handler) {
                delay(1000)
                doSomethingUsefulTwo()
            }
            joinAll(job1, job2)
        }
        Log.d(TAG, "All DONE!")
    }

    /**
     * Utilizzare SupervisorJob o supervisorScope
     * quando NON si desidera che un errore annulli il genitore e i fratelli
     * quindi se child#1 fallisce, né child#2 né lo scope verrà annullato.
     * Se un figlio di SupervisorJob genera un'eccezione, questo non propagherà l'eccezione
     * nella gerarchia e consentirà alla sua coroutine di gestirla.
     * Con launch, le eccezioni verranno lanciate non appena si verificano (try/catch o runCatching)
     * mentre con async usato come figlio diretto di un'istanza CoroutineScopeo supervisorScope,
     * le eccezioni non vengono lanciate automaticamente, vengono lanciate quando chiami .await()
     * perciò per gestire le eccezioni generate da una async eseguita come root coroutine,
     * puoi avvolgere la .await() all'interno di un try/catch
     *
     * https://medium.com/androiddevelopers/exceptions-in-coroutines-ce8da1ec060c
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `dont want a failure cancel parent or siblings 2 version`() = runTest {
        val scope = CoroutineScope(SupervisorJob())
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "CoroutineExceptionHandler catched: $exception")
        }
        Log.d(TAG, "launch doSomethingUsefulOne")
        val job1 = scope.launch(handler) {
            delay(500)
            doSomethingUsefulOne(success = false)
            Log.d(TAG, "will not be executed: doSomething after a fail in this scope!!!")
            doSomethingUsefulOne()
        }
        Log.d(TAG, "launch doSomethingUsefulTwo")
        val job2 = scope.launch(handler) {
            delay(1000)
            doSomethingUsefulTwo()
        }
        joinAll(job1, job2)
        Log.d(TAG, "All DONE!")
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exception in nested async`() = runTest {
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            val deferred = async {
                try {
                    doSomethingUsefulOne(success = false)
                } catch (exc: Exception) {
                    Log.e(TAG, "Error occurred: $exc")
                    -1
                }
            }
            val usefulOne = deferred.await()

            delay(150L)
            val usefulTwo = doSomethingUsefulTwo()
            Log.d(TAG, "Results: $usefulOne, $usefulTwo")
        }
        job.join()
        Log.d(TAG, "All DONE!")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exception in root async with SupervisorJob`() = runTest {
        supervisorScope {
            val deferred = async { doSomethingUsefulOne(success = false) }
            val usefulOne = try {
                deferred.await()
            } catch (exc: Exception) {
                Log.e(TAG, "Error occurred: $exc")
                -1
            }

            delay(150L)
            val usefulTwo = async { doSomethingUsefulTwo() }.await()
            Log.d(TAG, "All DONE, results: $usefulOne, $usefulTwo")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exception in root async`() = runTest {
        val scope = CoroutineScope(Job())
        val deferred = scope.async { doSomethingUsefulOne(success = false) }
        val usefulOne = try {
            deferred.await()
        } catch (exc: Exception) {
            Log.e(TAG, "Error occurred: $exc")
            -1
        }

        delay(150L)
        val usefulTwo = doSomethingUsefulTwo()
        Log.d(TAG, "All DONE, results: $usefulOne, $usefulTwo")
    }

}