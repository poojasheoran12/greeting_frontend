package com.example.greeting.presentation.preview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.greeting.presentation.core.components.PremiumButton
import com.example.greeting.presentation.preview.components.GreetingPreviewContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    onBackClick: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PreviewEvent.Error -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                PreviewEvent.ShareComplete -> Toast.makeText(context, "Greeting Shared!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val template = uiState.template
            val userProfile = uiState.userProfile

            if (template != null && userProfile != null) {
                Card(
                    modifier = Modifier.weight(1f).aspectRatio(9f / 16f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    GreetingPreviewContent(template = template, userProfile = userProfile)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                PremiumButton(
                    text = "Share with Friends",
                    onClick = { viewModel.onShareClick() },
                    isLoading = uiState.isSharing,
                    icon = Icons.Default.Share
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
