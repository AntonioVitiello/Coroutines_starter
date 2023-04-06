package it.av.coroutines_core

import kotlinx.coroutines.*
import org.junit.Test

/**
 * Created by Antonio Vitiello on 06/04/2023.
 *
 * https://medium.com/androiddevelopers/coroutines-first-things-first-e6187bf3bb21
 */
class CoroutineCancellation {
    companion object {
        const val TAG = "CoroutineCancellation"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `non cooperative coroutine code`(): Unit = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) {
                if (System.currentTimeMillis() >= nextPrintTime) {
                    Log.d(TAG, "Hello ${i++}")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1_000L)
        Log.d(TAG, "Cancel!")
        job.cancel()
        Log.d(TAG, "Done!")
    }


    /**
     * Cooperative Coroutine code for Cancellation
     * Method #1: Job.isActive that consent to do an action before finish coroutine
     * ensureActive() is an alternative to isActive that istantaneously stop work throwing CancellationException
     */
    @Test
    fun `cooperative coroutine code #1`(): Unit = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            try {
                var nextPrintTime = startTime
                var i = 0
                // while (i < 5 && isActive) { // isActive: do an action before finish coroutine
                while (i < 5) {
                    ensureActive() // ensureActive: istantaneously stop work throwing CancellationException
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        Log.d(TAG, "Hello ${i++}")
                        nextPrintTime += 500L
                    }
                }
                if (!isActive) {
                    Log.d(TAG, "Clean up!")
                }
            } catch (exc: CancellationException) {
                Log.e(TAG, "Error occurred: $exc")
            } finally {
                Log.e(TAG, "Work cancelled!")
            }
        }
        delay(1_000L)
        Log.d(TAG, "Cancel!")
        job.cancel()
        Log.d(TAG, "Done!")
    }

    /**
     * Cooperative Coroutine code for Cancellation
     * Method #2: usind suspend function Job.yield
     * yield calls ensureActive() that throws CancellationException and cancel Job istantaneously
     * yield is something rhat you should use when doing a CPU heavy computation that may axhaust thread-pool
     */
    @Test
    fun `cooperative coroutine code #2`(): Unit = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            try {
                var nextPrintTime = startTime
                var i = 0
                while (i < 5) {
                    yield() // throws CancellationException (calls ensureActive())
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        Log.d(TAG, "Hello ${i++}")
                        nextPrintTime += 500L
                    }
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Error occurred: $exc")
            }
        }
        delay(1_000L)
        Log.d(TAG, "Cancel!")
        job.cancel()
        Log.d(TAG, "Done!")
    }

    /**
     * Adding a delay to loop, don't need isActive, ensureActive() or yeld!
     * becouse delay return a suspendCancellableCoroutine
     * REMEMBER: All the suspending functions in kotlinx.coroutines are cancellable!
     * Job.join() è particolarmente utile nei test per essere sicuri che la coroutine venga eseguita sino alla fine
     * Se chiamo cancel() prima di join() non c'è nessuna azione
     * Se invece chiamo cancel() prima di .await() throws CancellationException (perchè è stata cancellata)
     */
    @Test
    fun `cooperative coroutine code #3`(): Unit = runBlocking {
        val job = launch(Dispatchers.Default) {
            try {
                repeat(5) { i: Int ->
                    Log.d(TAG, "Hello $i")
                    delay(500L)
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Error occurred: $exc")
            }
        }
        delay(1_000L)
        // job.join() // join blocca l'esecuzione fino al termine della coroutine Job
        Log.d(TAG, "Cancel!")
        job.cancel()
        // job.join()
        Log.d(TAG, "Done!")
    }

}