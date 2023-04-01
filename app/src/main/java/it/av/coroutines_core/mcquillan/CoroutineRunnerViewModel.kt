package it.av.coroutines_core.mcquillan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Antonio Vitiello on 31/03/2023.
 */
class CoroutineRunnerViewModel(application: Application) : AndroidViewModel(application) {
    private val singleRunner = SingleRunner()
    private val controlledRunner = ControlledRunner<ResourceModel>()
    private val atomicInteger = AtomicInteger(0)
    private var currentOperationId = 0

    private val _disableButtonLiveData = MutableLiveData<Boolean>()
    val disableButtonLiveData: LiveData<Boolean> = _disableButtonLiveData

    private val _showProgressLiveData = MutableLiveData<Boolean>()
    val showProgressLiveData: LiveData<Boolean> = _showProgressLiveData

    companion object {
        const val ID_ONE_TIME_CLICK = 1
        const val ID_SINGLE_RUNNER = 2
        const val ID_JOIN_PREVIOUS = 3
        const val ID_CANCEL_PREVIOUS = 4
    }

    suspend fun loadDataOneTimeClick(): ResourceModel {
        _disableButtonLiveData.value = true
        _showProgressLiveData.value = true
        delay(1000L)
        return try {
            val counter = getCounterForOperation(ID_ONE_TIME_CLICK)
            ResourceModel(Resource.Success("OneTimeClick: $counter"), counter == 1)
        } finally {
            _showProgressLiveData.value = false
            _disableButtonLiveData.value = false
        }
    }

    suspend fun loadDataWithSingleRunner(): ResourceModel {
        _showProgressLiveData.value = true
        // wait for the previous sort to complete before starting a new one
        val model = singleRunner.afterPrevious {
            delay(1000L)
            val counter = getCounterForOperation(ID_SINGLE_RUNNER)
            ResourceModel(Resource.Success("SingleRunner: $counter"), counter == 1)
        }
        _showProgressLiveData.value = false
        return model
    }

    suspend fun loadDataWithJoinPreviousOrRun(): ResourceModel {
        _showProgressLiveData.value = true
        // Join Previous coroutine Or Run new one
        val model = controlledRunner.joinPreviousOrRun {
            delay(1000L)
            val counter = getCounterForOperation(ID_JOIN_PREVIOUS)
            // set live data only if the data is changed!
            ResourceModel(Resource.Success("JoinPreviousOrRun: $counter"), counter == 1)
        }
        _showProgressLiveData.value = false
        return model
    }

    suspend fun loadDataWithCancelPreviousThenRun(): ResourceModel {
        _showProgressLiveData.value = true
        // Cancel Previous coroutine Then Run new one
        val model = controlledRunner.cancelPreviousThenRun {
            delay(1000L)
            val counter = getCounterForOperation(ID_CANCEL_PREVIOUS)
            ResourceModel(Resource.Success("CancelPreviousThenRun: $counter"), counter == 1)
        }
        _showProgressLiveData.value = false
        return model
    }

    private fun getCounterForOperation(operationId: Int): Int {
        if (currentOperationId != operationId) {
            atomicInteger.set(0)
        }
        currentOperationId = operationId
        return atomicInteger.addAndGet(1)
    }

}