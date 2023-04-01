package it.av.coroutines_core.mcquillan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Created by Antonio Vitiello on 31/03/2023.
 */
class CoroutineRunnerViewModel(application: Application) : AndroidViewModel(application) {
    private val singleRunner = SingleRunner()
    private val controlledRunner = ControlledRunner<ResourceModel>()
    private val repository = Repository()

    private val _disableButtonLiveData = MutableLiveData<Boolean>()
    val disableButtonLiveData: LiveData<Boolean> = _disableButtonLiveData

    companion object {
        const val ID_ONE_TIME_CLICK = 1
        const val ID_SINGLE_RUNNER = 2
        const val ID_JOIN_PREVIOUS = 3
        const val ID_CANCEL_PREVIOUS = 4
    }

    suspend fun loadDataOneTimeClick(): ResourceModel {
        _disableButtonLiveData.value = true
        return try {
            repository.loadData(ID_ONE_TIME_CLICK)
        } finally {
            _disableButtonLiveData.value = false
        }
    }

    suspend fun loadDataWithSingleRunner(): ResourceModel {
        // wait for the previous sort to complete before starting a new one
        val model = singleRunner.afterPrevious {
            repository.loadData(ID_SINGLE_RUNNER)
        }
        return model
    }

    suspend fun loadDataWithJoinPreviousOrRun(): ResourceModel {
        // Join Previous coroutine Or Run new one
        val model = controlledRunner.joinPreviousOrRun {
            // set live data only if the data is changed!
            repository.loadData(ID_JOIN_PREVIOUS)
        }
        return model
    }

    suspend fun loadDataWithCancelPreviousThenRun(): ResourceModel {
        // Cancel Previous coroutine Then Run new one
        val model = controlledRunner.cancelPreviousThenRun {
            repository.loadData(ID_CANCEL_PREVIOUS)
        }
        return model
    }

}