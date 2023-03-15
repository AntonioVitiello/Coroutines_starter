package it.av.coroutines_core

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Created by Antonio Vitiello on 10/03/2023.
 */
class ComposingSuspendingFun {
    companion object {
        const val TAG = "ComposingSuspendingFun"
    }

    @Test
    fun `Execute Sequential suspending fun`() = runBlocking {
        val time = measureTimeMillis {
            val one = doSomethingUsefulOne()
            val two = doSomethingUsefulTwo()
            Log.d(TAG, "The answer is ${one + two}")
        }
        Log.d(TAG, "Completed in $time ms")
    }


    @Test
    fun `Execute Concurrent suspending fun`() = runBlocking {
        val time = measureTimeMillis {
            val one = async { doSomethingUsefulOne() }
            val two = async { doSomethingUsefulTwo() }
            Log.d(TAG, "The answer is ${one.await() + two.await()}")
        }
        Log.d(TAG, "Completed in $time ms")
    }


    @Test
    fun `Execute Parallel suspending fun`() = runBlocking {
        val time = measureTimeMillis {
            val one = async { doSomethingUsefulOne() }
            val two = async { doSomethingUsefulTwo() }
            Log.d(TAG, "The answer is ${one.await() + two.await()}")
        }
        Log.d(TAG, "Completed in $time ms")
    }


    @Test
    fun `Execute Parallel suspending fun in IO Thread`() = runBlocking {
        val time = measureTimeMillis {
            withContext(Dispatchers.IO) {
                val one = async { doSomethingUsefulOne() }
                val two = async { doSomethingUsefulTwo() }
                Log.d(TAG, "The answer is ${one.await() + two.await()}")
            }
        }
        Log.d(TAG, "Completed in $time ms")
    }


    @Test
    fun `Execute LAZY Concurrent suspending fun`() = runBlocking {
        val time = measureTimeMillis {
            val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
            val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
            // some computation
            one.start() // start the first one
            two.start() // start the second one
            Log.d(TAG, "The answer is ${one.await() + two.await()}")
        }
        Log.d(TAG, "Completed in $time ms")
    }


    /**
     * This programming style with async functions is strongly discouraged:
     * if between somethingUsefulOneAsync() is some logic error throws an exception and the operation aborts
     * but somethingUsefulTwoAsync still running in the background
     */
    @Test
    fun `Execute async-style fun using GlobalScope`() {
        val time = measureTimeMillis {
            // we can initiate async actions outside of a coroutine
            // i.e. we don't have `runBlocking` to the right of this fun
            val one = somethingUsefulOneAsync()
            val two = somethingUsefulTwoAsync()
            // but waiting for a result must involve either suspending or blocking.
            // here we use `runBlocking { ... }` to block the main thread while waiting for the result
            runBlocking {
                Log.d(TAG, "The answer is ${one.await() + two.await()}")
            }
        }
        Log.d(TAG, "Completed in $time ms")
    }

    // The result type of somethingUsefulOneAsync is Deferred<Int>
    //Async suffix needed
    @OptIn(DelicateCoroutinesApi::class)
    private fun somethingUsefulOneAsync() = GlobalScope.async {
        doSomethingUsefulOne()
    }

    // The result type of somethingUsefulTwoAsync is Deferred<Int>
    //Async suffix needed
    @OptIn(DelicateCoroutinesApi::class)
    private fun somethingUsefulTwoAsync() = GlobalScope.async {
        doSomethingUsefulTwo()
    }


    private suspend fun doSomethingUsefulOne(): Int {
        Log.d(TAG, "doing something useful: one")
        delay(1000L) // pretend we are doing something useful here
        return 13
    }

    private suspend fun doSomethingUsefulTwo(): Int {
        Log.d(TAG, "doing something useful: two")
        delay(1000L) // pretend we are doing something useful here, too
        return 29
    }

}