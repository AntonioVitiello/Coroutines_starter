package it.av.coroutines_core

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_launch.*
import kotlinx.coroutines.*

/**
 * see video:
 * https://www.youtube.com/watch?v=Cq3di5lfMkY&t=1203s
 */
class LaunchActivity : AppCompatActivity() {
    companion object {
        const val TAG = "AAA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        val result = runCatching {
            //startTest01(2)
            //startTest02(2)
            startTest03(2)
        }
        if (result.isFailure) {
            Log.e(TAG, "Si è verificato un errore", result.exceptionOrNull())
        }
    }

    private fun startTest01(ripCount: Int) {
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount.toString())
        Log.i(TAG, "startTest01\t[${Thread.currentThread().name}]")
        lifecycleScope.async(Dispatchers.IO) {
            repeat(ripCount) {
                startCounter().await()
            }
        }
    }

    private fun startCounter() = lifecycleScope.async(Dispatchers.Main) {
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
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount.toString())
        Log.i(TAG, "startTest02\t[${Thread.currentThread().name}]")
        lifecycleScope.launch {
            repeat(ripCount) {
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
        ripeatText.text = getString(R.string.ripetizioni_msg, ripCount.toString())
        Log.i(TAG, "startTest03\t[${Thread.currentThread().name}]")
        lifecycleScope.launch {
            for (j in 1..ripCount) {
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
