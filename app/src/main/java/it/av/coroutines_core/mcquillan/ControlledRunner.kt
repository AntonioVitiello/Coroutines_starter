package it.av.coroutines_core.mcquillan

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference

/**
 * A controlled runner decides what to do when new tasks are run:
 * [joinPreviousOrRun] the new task will be discarded and the result of the previous task will be returned
 * [cancelPreviousThenRun] the old task will be cancelled and then the new task will be run
 * see:
 * https://medium.com/androiddevelopers/coroutines-on-android-part-iii-real-work-2ba8a2ec2f45
 * https://gist.github.com/objcode/7ab4e7b1df8acd88696cb0ccecad16f7#file-concurrencyhelpers-kt-L19
 */
class ControlledRunner<T> {
    private val activeTask = AtomicReference<Deferred<T>?>(null)

    /**
     * Cancel all previous tasks before calling block.
     * @param block the code to run after previous work is cancelled.
     * @return the result of block, if this call was not cancelled prior to returning.
     */
    suspend fun cancelPreviousThenRun(block: suspend () -> T): T {
        activeTask.get()?.cancelAndJoin()
        return coroutineScope {
            val newTask = async(start = CoroutineStart.LAZY) {
                block()
            }
            newTask.invokeOnCompletion { // newTask is completed
                // if activeTask == newTask set activeTask = null atomically
                activeTask.compareAndSet(newTask, null)
            }

            val result: T
            while (true) {
                // if activeTask == null set activeTask = newTask atomically
                if (activeTask.compareAndSet(null, newTask)) {
                    result = newTask.await()
                    break
                } else {
                    activeTask.get()?.cancelAndJoin()
                    yield()
                }
            }

            result
        }
    }

    /**
     * [jonPreviousOrRun] If a previous block is running in a coroutine don't run the new block,
     * instead wait for the previous block and return it's result.
     * @param block the code to run if and only if no other task is currently running
     * @return the result of block, or if another task was running the result of that task instead.
     */
    suspend fun joinPreviousOrRun(block: suspend () -> T): T {
        activeTask.get()?.let {
            return it.await()
        }
        return coroutineScope {
            val newTask = async(start = CoroutineStart.LAZY) {
                block()
            }
            newTask.invokeOnCompletion { // newTask is completed
                // if activeTask == newTask set activeTask = null atomically
                activeTask.compareAndSet(newTask, null)
            }

            val result: T
            while (true) {
                // if activeTask == null set activeTask = newTask atomically
                if (activeTask.compareAndSet(null, newTask)) {
                    result = newTask.await()
                    break
                } else {
                    val currentTask = activeTask.get()
                    if (currentTask != null) {
                        newTask.cancel()
                        result = currentTask.await()
                        break
                    } else {
                        yield()
                    }
                }
            }

            result
        }
    }

}