package it.av.coroutines_core.mcquillan

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CancellationException

/**
 * Created by Antonio Vitiello on 31/03/2023.
 */
class CoroutineRunnerViewModel(application: Application) : AndroidViewModel(application) {
    private val singleRunner = SingleRunner()
    private val controlledRunner = ControlledRunner<DataModel?>()
    private val repository = Repository()

    private val _disableButtonLiveData = MutableLiveData<Boolean>()
    val disableButtonLiveData: LiveData<Boolean> = _disableButtonLiveData

    private val _errorLiveData = MutableLiveData<DataModel>()
    val errorLiveData: LiveData<DataModel> = _errorLiveData

    companion object {
        const val TAG = "CoroutineRunnerViewMode"
        const val ID_ONE_TIME_CLICK = 1
        const val ID_SINGLE_RUNNER = 2
        const val ID_JOIN_PREVIOUS = 3
        const val ID_CANCEL_PREVIOUS = 4
    }

    suspend fun loadDataOneTimeClick(): DataModel? {
        _disableButtonLiveData.value = true
        return try {
            val resourceModel = repository.loadData(ID_ONE_TIME_CLICK)
            manageResponse(resourceModel)
        } catch (exc: Exception) {
            manageException(exc)
        } finally {
            _disableButtonLiveData.value = false
        }
    }

    suspend fun loadDataWithSingleRunner(): DataModel? {
        // wait for the previous sort to complete before starting a new one
        val dataModel = singleRunner.afterPrevious {
            try {
                val resourceModel = repository.loadData(ID_SINGLE_RUNNER)
                manageResponse(resourceModel)
            } catch (exc: Exception) {
                manageException(exc)
            }
        }
        return dataModel
    }

    suspend fun loadDataWithJoinPreviousOrRun(): DataModel? {
        // Join Previous coroutine Or Run new one
        val dataModel = controlledRunner.joinPreviousOrRun {
            try {
                val resourceModel = repository.loadData(ID_JOIN_PREVIOUS)
                manageResponse(resourceModel)
            } catch (exc: Exception) {
                manageException(exc)
            }
        }
        return dataModel
    }

    suspend fun loadDataWithCancelPreviousThenRun(): DataModel? {
        // Cancel Previous coroutine Then Run new one
        val dataModel = controlledRunner.cancelPreviousThenRun {
            try {
                val resourceModel = repository.loadData(ID_CANCEL_PREVIOUS)
                manageResponse(resourceModel)
            } catch (cancExc: CancellationException) {
                Log.e(TAG, "CancellationException", cancExc)
                null
            } catch (exc: Exception) {
                manageException(exc)
            }
        }
        return dataModel
    }

    private fun manageResponse(model: ResourceModel): DataModel? {
        return if (model.resource is Resource.Success) {
            DataModel(model.resource.data ?: "--", model.clean)
        } else {
            Log.e(TAG, model.resource.errorMessage ?: "Network Error!")
            val errorDataModel = DataModel("Si è verificato un errore di rete, riprova più tardi.", false)
            _errorLiveData.value = errorDataModel
            null
        }
    }

    private fun manageException(exc: Exception): DataModel? {
        Log.e(TAG, "Application Error!", exc)
        val errorDataModel = DataModel("Si è verificato un errore, riprova più tardi.", false)
        _errorLiveData.value = errorDataModel
        return null
    }

}