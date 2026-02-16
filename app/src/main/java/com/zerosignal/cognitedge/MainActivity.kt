package com.zerosignal.cognitedge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zerosignal.cognitedge.logging.ZSLog
import com.zerosignal.cognitedge.ui.navigation.NavGraph
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZSLog.i(TAG, "onCreate")
        setContent {
            ZeroSignalTheme {
                NavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ZSLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        ZSLog.d(TAG, "onPause")
    }

    override fun onDestroy() {
        ZSLog.i(TAG, "onDestroy")
        super.onDestroy()
    }
}
