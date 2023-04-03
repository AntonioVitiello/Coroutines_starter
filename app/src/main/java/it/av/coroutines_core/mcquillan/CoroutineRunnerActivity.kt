package it.av.coroutines_core.mcquillan

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.av.coroutines_core.R
import kotlinx.android.synthetic.main.activity_coroutine_runner.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CoroutineRunnerActivity : AppCompatActivity() {
    private val viewModel: CoroutineRunnerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_runner)

        viewModel.disableButtonLiveData.observe(this, ::disableOneTimeButton)
        viewModel.errorLiveData.observe(this, ::showErrorDialog)

        initContents()
    }

    private fun initContents() {
        oneTimeButton.setOnClickListener { onOneTimeClickResult() }
        singleRunnerButton.setOnClickListener { onLoadDataSingleRunnerResult() }
        joinPreviousOrRunButton.setOnClickListener { onJoinPreviousOrRunResult() }
        cancelPreviousThenRunButton.setOnClickListener { onCancelPreviousThenRunResult() }
    }

    private fun onOneTimeClickResult() {
        lifecycleScope.launch {
            showProgress(true)
            val model = viewModel.loadDataOneTimeClick()
            showData(model)
            showProgress(false)
        }
    }

    private fun onLoadDataSingleRunnerResult() {
// CoroutineExceptionHandler sample
//        val handler = CoroutineExceptionHandler { _, exception ->
//            Log.e("CoroutineRunnerActivity", "CoroutineExceptionHandler got $exception")
//        }
//        lifecycleScope.launch(handler) {
//            throw AssertionError()
//        }
        lifecycleScope.launch {
            showProgress(true)
            val model = viewModel.loadDataWithSingleRunner()
            showData(model)
            showProgress(false)
        }
    }

    private fun onJoinPreviousOrRunResult() {
        lifecycleScope.launch {
            showProgress(true)
            val model = viewModel.loadDataWithJoinPreviousOrRun()
            showData(model)
            showProgress(false)
        }
    }

    private fun onCancelPreviousThenRunResult() {
        lifecycleScope.launch {
            showProgress(true)
            val model = viewModel.loadDataWithCancelPreviousThenRun()
            showData(model)
            showProgress(false)
        }
    }

    private fun showData(dataModel: DataModel?) {
        dataModel?.let { model ->
            val msg = if (model.clean) {
                model.message
            } else {
                "${resultText.text}\n${model.message}"
            }
            resultText.text = msg
        }
    }

    private fun disableOneTimeButton(disable: Boolean) {
        oneTimeButton.isEnabled = !disable
    }

    private fun showProgress(visible: Boolean) {
        if (visible) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    private fun showErrorDialog(dataModel: DataModel) {
        AlertDialog.Builder(this)
            .setMessage(dataModel.message)
            .setPositiveButton(getString(android.R.string.ok), null)
            .setCancelable(false)
            .show()
    }

}