package it.av.coroutines_core.mcquillan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Antonio Vitiello on 31/03/2023.
 */
class CoroutineRunnerViewModel(application: Application) : AndroidViewModel(application) {
    private val singleRunner = SingleRunner()
    private val controlledRunner = ControlledRunner<ResourceModel>()
    private val atomicInteger = AtomicInteger(0)
    private var currentOperationId = 0

    private val _disableButtonsLiveData = MutableLiveData<Boolean>()
    val disableButtonsLiveData: LiveData<Boolean> = _disableButtonsLiveData

    private val _resourceLiveData = MutableLiveData<ResourceModel>()
    val resourceLiveData: LiveData<ResourceModel> = _resourceLiveData

    companion object {
        const val ID_ONE_TIME_CLICK = 1
        const val ID_SINGLE_RUNNER = 2
        const val ID_JOIN_PREVIOUS = 3
        const val ID_CANCEL_PREVIOUS = 4
    }

    fun loadDataOneTimeClick() {
        viewModelScope.launch {
            _disableButtonsLiveData.value = true
            delay(1000L)
            try {
                val counter = getCounterForOperation(ID_ONE_TIME_CLICK)
                val model = ResourceModel(Resource.Success("OneTimeClick: $counter"), counter == 1)
                _resourceLiveData.value = model
            } finally {
                _disableButtonsLiveData.value = false
            }
        }
    }

    suspend fun loadDataWithSingleRunner() {
        // wait for the previous sort to complete before starting a new one
        return singleRunner.afterPrevious {
            delay(1000L)
            val counter = getCounterForOperation(ID_SINGLE_RUNNER)
            val model = ResourceModel(Resource.Success("SingleRunner: $counter"), counter == 1)
            _resourceLiveData.value = model
        }
    }

    suspend fun loadDataWithJoinPreviousOrRun() {
        // Join Previous coroutine Or Run new one
        val model = controlledRunner.joinPreviousOrRun {
            delay(1000L)
            val counter = getCounterForOperation(ID_JOIN_PREVIOUS)
            ResourceModel(Resource.Success("JoinPreviousOrRun: $counter"), counter == 1)
        }
        if (_resourceLiveData.value?.resource?.data != model.resource.data) {
            // set live data only if the data is changed!
            _resourceLiveData.value = model
        }
    }

    suspend fun loadDataWithCancelPreviousThenRun() {
        // Cancel Previous coroutine Then Run new one
        val model = controlledRunner.cancelPreviousThenRun {
            delay(1000L)
            val counter = getCounterForOperation(ID_CANCEL_PREVIOUS)
            ResourceModel(Resource.Success("CancelPreviousThenRun: $counter"), counter == 1)
        }
        _resourceLiveData.value = model
    }

    private fun getCounterForOperation(operationId: Int): Int {
        if (currentOperationId != operationId) {
            atomicInteger.set(0)
        }
        currentOperationId = operationId
        return atomicInteger.addAndGet(1)
    }

}