package com.avans.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.avans.gymtracker.ui.navigation.AppNavGraph
import com.avans.gymtracker.ui.theme.GymTrackerTheme
import com.avans.gymtracker.utils.PreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(this)

        setContent {
            GymTrackerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        prefs = prefs,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
