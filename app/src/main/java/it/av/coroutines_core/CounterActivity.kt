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
            lifecycleScope.launch(Dispatchers.Main) {
                val job1 = launch {
                    startTest01(2)
                    delay(11000)
                }

                job1.join()
                val job2 = launch(Dispatchers.Main) {
                    startTest02(2)
                    delay(12000)
                }

                job2.join()
                val job3 = lifecycleScope.launch(Dispatchers.Main) {
                    startTest03(2)
                    delay(13000)
                }

                job3.join()
                lifecycleScope.launch(Dispatchers.Main) {
                    finalText.isVisible = true
                }
            }
        }

        if (result.isFailure) {
            Log.e(TAG, "Si è verificato un errore", result.exceptionOrNull())
        }
    }

    private fun startTest01(ripCount: Int) {
        testText.text = getString(R.string.test_msg, "01")
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount)
        Log.i(TAG, "startTest01\t[${Thread.currentThread().name}]")
        val deferred = lifecycleScope.async {
            repeat(ripCount) { i: Int ->
                stepText.text = getString(R.string.step_msg, i + 1)
                startCounter1().await()
            }
        }
    }

    private fun startCounter1() = lifecycleScope.async(Dispatchers.Main) {
        for (i in 5 downTo 0) {
            counterText.text = i.toString()
            delay(1000L)
        }
    }

    private fun startCounter2() = lifecycleScope.async {
        withContext(Dispatchers.Main) {
            for (i in 5 downTo 0) {
                counterText.text = i.toString()
                delay(1000L)
            }
        }
    }

    private fun startTest02(ripCount: Int) {
        testText.text = getString(R.string.test_msg, "02")
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount)
        Log.i(TAG, "startTest02\t[${Thread.currentThread().name}]")

        lifecycleScope.launch {
            repeat(ripCount) { j: Int ->
                stepText.text = getString(R.string.step_msg, j + 1)
                Log.i(TAG, "repeat\t[${Thread.currentThread().name}]")
                for (i in 5 downTo 0) {
                    val counter = getCounter(i).await()
                    Log.i(TAG, "counter\t[${Thread.currentThread().name}]")
                    counterText.text = counter.toString()
                }
            }
        }
    }

    private fun getCounter(index: Int) = lifecycleScope.async(Dispatchers.IO) {
        Log.i(TAG, "getCounter\t[${Thread.currentThread().name}]")
        delay(1000L)
        return@async index
    }

    private fun startTest03(ripCount: Int) {
        testText.text = getString(R.string.test_msg, "03")
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount)
        Log.i(TAG, "startTest03\t[${Thread.currentThread().name}]")
        lifecycleScope.launch {
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
