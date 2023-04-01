package it.av.coroutines_core.mcquillan

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Antonio Vitiello on 01/04/2023.
 */
class Repository {
    private val atomicInteger = AtomicInteger(0)
    private var currentOperationId = 0

    fun loadData(operationId: Int): ResourceModel {
        val counter = getCounterForOperation(operationId)
        return ResourceModel(Resource.Success("CancelPreviousThenRun: $counter"), counter == 1)
    }

    private fun getCounterForOperation(operationId: Int): Int {
        if (currentOperationId != operationId) {
            atomicInteger.set(0)
        }
        currentOperationId = operationId
        return atomicInteger.addAndGet(1)
    }

}