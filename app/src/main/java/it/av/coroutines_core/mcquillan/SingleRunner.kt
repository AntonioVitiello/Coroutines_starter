package it.av.coroutines_core.mcquillan

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Antonio Vitiello on 31/03/2023.
 */
/**
 * Execute Tasks Sequentially in Coroutines
 * Calling [afterPrevious] will always ensure that all previously requested work completes prior to
 * calling the block passed. Any future calls to [afterPrevious] while the current block is running
 * will wait for the current block to complete before starting.
 */
class SingleRunner {
    private val mutex = Mutex()

    /**
     * Ensure that the block will only be executed after all previous work has completed.
     * When several coroutines call afterPrevious at the same time, they will queue up in the order
     * that they call afterPrevious. Then, one coroutine will enter the block at a time.
     * @param block the code to run after previous work is complete.
     */
    suspend fun <T> afterPrevious(block: suspend () -> T): T {
        mutex.withLock {
            return block()
        }
    }

}