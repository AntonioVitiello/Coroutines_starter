package it.av.coroutines_core

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Created by Antonio Vitiello on 10/03/2023.
 */
class ContextAndDispatchers {
    companion object {
        const val TAG = "ContextAndDispatchers"
    }

    @Test
    fun `Jumping between threads`() = runBlocking {
        val time = measureTimeMillis {
            launch(Dispatchers.IO) { // not confined -- will work with main thread
                Log.d(TAG, "I'm working in thread ${Thread.currentThread().name}")
            }
            delay(1000)
            launch { // not confined -- will work with main thread
                Log.d(TAG, "After delay I'm working in thread ${Thread.currentThread().name}")
            }
        }
        Log.d(TAG, "Completed in $time ms")
    }

    @Test
    fun dosomething() = runBlocking {
        val time = measureTimeMillis {
            launch { // not confined -- will work with main thread
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "I'm working in thread ${Thread.currentThread().name}")
                }
                delay(1000)
                Log.d(TAG, "After delay in thread ${Thread.currentThread().name}")
            }
        }
        Log.d(TAG, "Completed in $time ms")
    }


    @Test
    fun `Parent coroutine waits for completion of all its children`() = runBlocking {
        val request = launch {
            repeat(3) { i -> // launch a few children jobs
                launch  {
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    Log.d(TAG, "Coroutine $i is done")
                }
            }
            Log.d(TAG, "Children are still active")
        }
        request.join() // wait for completion of the request, including all its children
        Log.d(TAG, "Children completion: processing is complete")
    }
    
    
    @Test
    fun `Combining context elements`() = runBlocking<Unit> {
        launch(Dispatchers.Default + CoroutineName("AntCoroutine")) {
            Log.d(TAG, "I'm working in thread ${Thread.currentThread().name}")
        }
    }
    
}