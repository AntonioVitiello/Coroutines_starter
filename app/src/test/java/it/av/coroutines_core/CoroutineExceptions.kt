package it.av.coroutines_core

import kotlinx.coroutines.*
import org.junit.Test

/**
 * Created by Antonio Vitiello on 10/03/2023.
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
    fun `Supervision scope exception handling`() = runBlocking {
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
        } catch(e: AssertionError) {
            Log.e(TAG, "Caught an assertion error", e)
        }
    }


    @Test
    fun main() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG, "CoroutineExceptionHandler got $exception")
        }
        supervisorScope {
            val child = launch(handler) {
                Log.d(TAG, "The child is throwing an exception")
                throw AssertionError()
            }
            Log.d(TAG, "The scope is completing")
        }
        Log.d(TAG, "The scope is completed")
    }

}