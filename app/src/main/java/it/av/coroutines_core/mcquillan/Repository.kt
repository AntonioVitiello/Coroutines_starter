package it.av.coroutines_core.mcquillan

import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_CANCEL_PREVIOUS
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_JOIN_PREVIOUS
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_ONE_TIME_CLICK
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_SINGLE_RUNNER
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Antonio Vitiello on 01/04/2023.
 */
class Repository {
    private val atomicInteger = AtomicInteger(0)
    private var currentOperationId = 0
    private val countForError = 3

    suspend fun loadData(operationId: Int): ResourceModel {
        delay(1000L)
        val counter = getCounterForOperation(operationId)
        val msg = when (operationId) {
            ID_ONE_TIME_CLICK -> "OneTimeClick: $counter"
            ID_SINGLE_RUNNER -> "SingleRunner: $counter"
            ID_JOIN_PREVIOUS -> "JoinPreviousOrRun: $counter"
            ID_CANCEL_PREVIOUS -> "CancelPreviousThenRun: $counter"
            else -> "undefined"
        }
        return ResourceModel(Resource.Success(msg), counter == 1)
    }

    private fun getCounterForOperation(operationId: Int): Int {
        if (atomicInteger.get() == countForError) {
            atomicInteger.set(countForError + 1)
            throw NumberFormatException("Error: TEST EXCEPTIONS!")
        }
        if (currentOperationId != operationId) {
            atomicInteger.set(0)
        }
        currentOperationId = operationId
        return atomicInteger.addAndGet(1)
    }

}