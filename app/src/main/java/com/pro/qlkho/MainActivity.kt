package com.pro.qlkho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pro.qlkho.ui.WmsApp
import com.pro.qlkho.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Extract SSO info from intent if launched from Synergy
        val ssoToken = intent.getStringExtra("auth_token")
        val ssoUsername = intent.getStringExtra("username")
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WmsApp(ssoToken = ssoToken, ssoUsername = ssoUsername)
            }
        }
    }
}
