package it.av.coroutines_core.mcquillan

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.av.coroutines_core.R
import kotlinx.android.synthetic.main.activity_coroutine_runner.*
import kotlinx.coroutines.launch

class CoroutineRunnerActivity : AppCompatActivity() {
    private val viewModel: CoroutineRunnerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_runner)

        viewModel.disableButtonLiveData.observe(this, ::disableOneTimeButton)

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

    private fun showData(model: ResourceModel) {
        if (model.resource is Resource.Success) {
            val msg = if (model.clean) {
                model.resource.data
            } else {
                "${resultText.text}\n${model.resource.data}"
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

}