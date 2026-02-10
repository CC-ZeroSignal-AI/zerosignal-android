package com.zerosignal.cognitedge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zerosignal.cognitedge.ui.navigation.NavGraph
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroSignalTheme {
                NavGraph()
            }
        }
    }
}
