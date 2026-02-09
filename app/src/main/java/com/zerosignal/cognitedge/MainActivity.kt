package com.zerosignal.cognitedge

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zerosignal.cognitedge.rag.RagViewModel
import com.zerosignal.cognitedge.rag.RagViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: RagViewModel by viewModels {
        RagViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val output = findViewById<TextView>(R.id.outputText)
        val input = findViewById<EditText>(R.id.inputText)
        val askButton = findViewById<Button>(R.id.askButton)

        askButton.setOnClickListener {
            viewModel.ask(input.text.toString())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when {
                        state.errorMessage != null -> output.text = state.errorMessage
                        state.isThinking -> output.text = "Thinking...\n${state.answer}"
                        else -> output.text = state.answer
                    }
                }
            }
        }
    }
}
