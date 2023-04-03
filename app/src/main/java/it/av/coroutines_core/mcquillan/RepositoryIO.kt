package it.av.coroutines_core.mcquillan

import android.util.Log
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_CANCEL_PREVIOUS
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_JOIN_PREVIOUS
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_ONE_TIME_CLICK
import it.av.coroutines_core.mcquillan.CoroutineRunnerViewModel.Companion.ID_SINGLE_RUNNER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Antonio Vitiello on 01/04/2023.
 */
class RepositoryIO {
    private val atomicInteger = AtomicInteger(0)
    private var currentOperationId = 0
    private val countForError = 3

    companion object{
        const val TAG = "RepositoryIO"
    }

    suspend fun loadData(operationId: Int): ResourceModel {
        Log.d(TAG, "Start loading data on Thread: ${Thread.currentThread().name}")
        delay(1000L)
        val counter = getCounterForOperation(operationId)
        val msg = withContext(Dispatchers.IO) {
            Log.d(TAG, "Loading data on Thread: ${Thread.currentThread().name}")
            when (operationId) {
                ID_ONE_TIME_CLICK -> "OneTimeClick: $counter"
                ID_SINGLE_RUNNER -> "SingleRunner: $counter"
                ID_JOIN_PREVIOUS -> "JoinPreviousOrRun: $counter"
                ID_CANCEL_PREVIOUS -> "CancelPreviousThenRun: $counter"
                else -> "undefined"
            }
        }
        Log.d(TAG, "Data loaded on Thread: ${Thread.currentThread().name}")
        return ResourceModel(Resource.Success(msg), counter == 1 && operationId != ID_JOIN_PREVIOUS)
    }

    private fun getCounterForOperation(operationId: Int): Int {
        if (currentOperationId != operationId) {
            atomicInteger.set(0)
        }
        if (atomicInteger.get() == countForError) {
            atomicInteger.set(countForError + 1)
            throw NumberFormatException("EXCEPTION TEST: countForError=$countForError")
        }
        currentOperationId = operationId
        return atomicInteger.addAndGet(1)
    }

}