package it.av.coroutines_core

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.*

/**
 * see video:
 * https://www.youtube.com/watch?v=Cq3di5lfMkY&t=1203s
 */
class CounterActivity : AppCompatActivity() {
    companion object {
        const val TAG = "AAA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        val result = runCatching {
            lifecycleScope.launch(Dispatchers.Main) { //attività consecutive
                startTest01(2)
                startTest02(2)
                startTest03(2)
                finalText.isVisible = true
            }
        }

        if (result.isFailure) {
            Log.e(TAG, "Si è verificato un errore", result.exceptionOrNull())
        }
    }

    private suspend fun startTest01(ripCount: Int) = coroutineScope {
        testText.text = getString(R.string.test_msg, "01")
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount)
        Log.i(TAG, "startTest01\t[${Thread.currentThread().name}]")
        val deferred = async {
            repeat(ripCount) { i: Int ->
                stepText.text = getString(R.string.step_msg, i + 1)
                startCounterAsync().await()
            }
        }
    }

    private fun startCounterAsync() = lifecycleScope.async(Dispatchers.Main) {
        for (i in 5 downTo 0) {
            counterText.text = i.toString()
            delay(1000L)
        }
    }

    private fun startCounterWithContextAsync() = lifecycleScope.async {
        withContext(Dispatchers.Main) {
            for (i in 5 downTo 0) {
                counterText.text = i.toString()
                delay(1000L)
            }
        }
    }

    private suspend fun startTest02(ripCount: Int) = coroutineScope {
        testText.text = getString(R.string.test_msg, "02")
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount)
        Log.i(TAG, "startTest02\t[${Thread.currentThread().name}]")

        launch {
            repeat(ripCount) { j: Int ->
                stepText.text = getString(R.string.step_msg, j + 1)
                Log.i(TAG, "repeat\t[${Thread.currentThread().name}]")
                for (i in 5 downTo 0) {
                    val counter = getCounterAsync(i).await()
                    Log.i(TAG, "counter\t[${Thread.currentThread().name}]")
                    counterText.text = counter.toString()
                }
            }
        }
    }

    private fun getCounterAsync(index: Int) = lifecycleScope.async(Dispatchers.IO) {
        Log.i(TAG, "getCounter\t[${Thread.currentThread().name}]")
        delay(1000L)
        return@async index
    }

    private suspend fun startTest03(ripCount: Int) = coroutineScope {
        testText.text = getString(R.string.test_msg, "03")
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount)
        Log.i(TAG, "startTest03\t[${Thread.currentThread().name}]")
        launch {
            for (j in 1..ripCount) {
                stepText.text = getString(R.string.step_msg, j)
                for (i in 5 downTo 0) {
                    var retVal: Int
                    val result = withContext(Dispatchers.IO) {
                        Log.i(TAG, "withContext\t[${Thread.currentThread().name}]")
                        retVal = i
                        delay(1000L)
                        retVal
                    }
                    counterText.text = result.toString()
                }
            }
        }
    }

}
