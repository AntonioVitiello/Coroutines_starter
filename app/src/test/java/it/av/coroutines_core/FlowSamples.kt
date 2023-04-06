package it.av.coroutines_core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.time.Duration

/**
 * Created by Antonio Vitiello on 05/04/2023.
 * see:
 * https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/topics/flow.md
 */
class FlowSamples {
    fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `flowOn operator collect in coroutine#1 and emit in coroutine#2`() = runTest {
        `simple with flowOn`().collect { value ->
            log("Collected $value\n")
        }
    }

    fun `simple with flowOn`(): Flow<Int> = flow {
        for (i in 1..3) {
            Thread.sleep(1500)
            log("Emitting $i")
            emit(i)
        }
    }.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder


    @Test
    fun `flowOn operator collect in coroutine#1 and emit in coroutine#2 #2`() = runTest {
        simple()
            .flowOn(Dispatchers.Default)
            .collect { value ->
                log("Collected $value\n")
            }
    }


    @Test
    fun `context preservation ie emit & collect in same context DefaultDispatcher`() = runTest {
        withContext(Dispatchers.Default) {
            simple().collect { value ->
                log("Collected $value\n")
            }
        }
    }

    fun simple(): Flow<Int> = flow {
        for (i in 1..3) {
            Thread.sleep(1500)
            log("Emitting $i")
            emit(i)
        }
    }

    fun `simple with delay`(): Flow<Int> = flow {
        for (i in 1..3) {
            delay(1_500L)
            log("Emitting $i")
            emit(i)
        }
    }


    @Test
    fun `collect in sequence`() = runBlocking {
        val time = measureTimeMillis {
            `simple with delay`().collect { value ->
                delay(300) // pretend we are processing it for 300 ms
                println(value)
            }
        }
        println("Collected in $time ms")
    }

    @Test
    fun `collect concurrently with buffer`() = runBlocking {
        val time = measureTimeMillis {
            `simple with delay`()
                .buffer()
                .collect { value ->
                delay(300) // pretend we are processing it for 300 ms
                println(value)
            }
        }
        println("Collected in $time ms")
    }

    @Test
    fun `collect with collectLatest`() = runBlocking {
        val time = measureTimeMillis {
            `simple with delay`()
                .collectLatest { value -> // cancel & restart on the latest value
                    println("Collecting $value")
                    delay(300) // pretend we are processing it for 300 ms
                    println("Done $value")
                }
        }
        println("Collected in $time ms")
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `collect with zip`() = runTest {
        val numbersFlow = (1..3).asFlow()
        val stringsFlow = flowOf("one", "two", "three")
        numbersFlow.zip(stringsFlow) { a, b -> "$a -> $b" }
            .collect { println(it) } // collect and print
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `collect with zip onEach`() = runBlocking {
        val time = measureTimeMillis {
            val numbersFlow = (1..3).asFlow().onEach { delay(400) }
            val stringsFlow = flowOf("one", "two", "three").onEach { delay(500) }
            numbersFlow.zip(stringsFlow) { a, b -> "$a -> $b" }
                .collect { println(it) }
        }
        println("Collected in $time ms")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `collect with combine`() = runBlocking {
        val time = measureTimeMillis {
            val numbersFlow = (1..3).asFlow().onEach { delay(400) }
            val stringsFlow = flowOf("one", "two", "three").onEach { delay(500) }
            numbersFlow.combine(stringsFlow) { a, b -> "$a -> $b" }
                .collect { println(it) }
        }
        println("Collected in $time ms")
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `ignore delays with runTest`() = runTest {
        println("start dataShouldBeHelloWorld")
        val data = fetchData()
        println(data)
    }

    suspend fun fetchData(): String {
        delay(Duration.parse("1h 30m"))
        return "Hello world"
    }

}