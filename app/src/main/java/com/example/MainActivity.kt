package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.AutomationViewModel
import com.example.ui.MainAppContainer
import com.example.ui.theme.SmsWithdrawTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AutomationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmsWithdrawTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}
