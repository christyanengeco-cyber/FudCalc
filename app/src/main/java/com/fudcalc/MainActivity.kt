package com.fudcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fudcalc.ui.AppViewModel
import com.fudcalc.ui.FudCalcApp
import com.fudcalc.ui.theme.FudCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FudCalcTheme {
                FudCalcApp(viewModel<AppViewModel>())
            }
        }
    }
}
