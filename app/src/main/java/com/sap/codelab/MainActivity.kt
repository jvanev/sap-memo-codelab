package com.sap.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sap.codelab.ui.screen.SAPMemoScreen
import com.sap.codelab.ui.theme.SAPMemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAPMemoTheme {
                SAPMemoScreen()
            }
        }
    }
}
