package com.example.greeting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.greeting.presentation.navigation.NavGraph
import com.example.greeting.presentation.theme.GreetingTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.greeting.core.utils.CacheCleanupWorker

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val cleanupWork = PeriodicWorkRequestBuilder<CacheCleanupWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CacheCleanup",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            cleanupWork
        )
        
        enableEdgeToEdge()
        setContent {
            GreetingTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}