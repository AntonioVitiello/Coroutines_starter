package it.av.coroutines_core

import kotlinx.coroutines.*
import org.junit.Test

/**
 * Created by Antonio Vitiello on 27/02/2023.
 * see:
 * https://kotlinlang.org/docs/cancellation-and-timeouts.html#cancelling-coroutine-execution
 */
class CancellationAndTimeout {
    companion object {
        const val TAG = "CancellationAndTimeout"
    }

    @Test
    fun `Cancel a cooperative delay`() = runBlocking {
        val job = launch {
            repeat(1000) { i ->
                Log.d(TAG, "job: I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // delay a bit
        Log.d(TAG, "main: I'm tired of waiting!")
        job.cancel() // cancels the job
        job.join() // waits for job's completion
        Log.d(TAG, "main: Now I can quit.")
    }


    @Test
    fun `Cancel fails if coroutine not cooperative`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) { // computation loop, just wastes CPU
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
    fun `Cancel fails if catch JobCancellationException`() = runBlocking {
        val job = launch(Dispatchers.Default) {
            repeat(5) { i ->
                try {
                    // print a message twice a second
                    Log.d(TAG, "job: I'm sleeping $i ...")
                    delay(500)
                } catch (e: Exception) {
                    // log the exception
                    Log.e(TAG, e)
                }
            }
        }
        delay(1300L) // delay a bit
        Log.d(TAG, "main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        Log.d(TAG, "main: Now I can quit.")
    }


    @Test
    fun `Cancel cooperative succeed`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            try {
                while (isActive) { // cancellable computation loop
                    // print a message twice a second
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        Log.d(TAG, "job: I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
                    }
                }
            } finally {
                Log.d(TAG, "finally: job ended!")
            }
        }
        delay(1300L) // delay a bit
        Log.d(TAG, "main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        Log.d(TAG, "main: Now I can quit.")
    }


    @Test
    fun `Timeout with TimeoutCancellationException`() = runBlocking {
        try {
            withTimeout(1300L) {
                repeat(1000) { i ->
                    Log.d(TAG, "I'm sleeping $i ...")
                    delay(500L)
                }
            }
        } catch (e: TimeoutCancellationException) {
            //Timeout: do nothing
        } finally {
            Log.d(TAG, "finally: job ended!")
        }
    }


    @Test
    fun `Timeout without TimeoutCancellationException`() = runBlocking {
        val result = withTimeoutOrNull(1300L) {
            repeat(1000) { i ->
                Log.d(TAG, "I'm sleeping $i ...")
                delay(500L)
            }
            "Done" // will get cancelled before it produces this result
        }
        Log.d(TAG, "Result: ${result ?: "Timeout reached!"}")
    }


    @Test
    fun `On Timeout Release resource acquired`() {
        var acquired = 0

        class Resource {
            init { // Acquire the resource
                acquired++
            }

            fun close() { // Release the resource
                acquired--
            }
        }

        runBlocking {
            repeat(10_000) { // Launch 10K coroutines
                launch {
                    var resource: Resource? = null // Not acquired yet
                    try {
                        withTimeout(60) { // Timeout of 60 ms
                            delay(50) // Delay for 50 ms
                            resource = Resource() // Store a resource to the variable if acquired
                        }
                        // ...do something with the resource acquired
                    } finally {
                        resource?.close() // Release the resource acquired!
                    }
                }
            }
        }
        // Outside of runBlocking all coroutines have completed
        Log.d(TAG, "Resource acquired: $acquired") // Print the number of resources still acquired
    }

}