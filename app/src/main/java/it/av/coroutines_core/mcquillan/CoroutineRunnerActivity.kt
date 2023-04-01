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

        viewModel.resourceLiveData.observe(this, ::showData)
        viewModel.disableButtonsLiveData.observe(this, ::disableOneTimeButton)

        initContents()
    }

    private fun initContents() {
        oneTimeButton.setOnClickListener {
            viewModel.loadDataOneTimeClick()
        }

        singleRunnerButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.loadDataWithSingleRunner()
            }
        }

        joinPreviousOrRunButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.loadDataWithJoinPreviousOrRun()
            }
        }

        cancelPreviousThenRunButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.loadDataWithCancelPreviousThenRun()
            }
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

}