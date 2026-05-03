package com.example.greeting.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.greeting.presentation.home.components.CategorySection
import com.example.greeting.presentation.home.components.FeaturedHeroSection
import com.example.greeting.presentation.home.components.HomeSectionItem
import com.example.greeting.presentation.home.components.PremiumUpsellDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPreview: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToPreview -> onNavigateToPreview(event.templateId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Greeting", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                FeaturedHeroSection()
            }

            items(uiState.sections, key = { it.title }) { section ->
                HomeSectionItem(
                    section = section,
                    onTemplateClick = { viewModel.onTemplateClick(it) }
                )
            }
        }
    }

    if (uiState.showPremiumDialog) {
        PremiumUpsellDialog(
            onDismiss = { viewModel.dismissPremiumDialog() },
            onSubscribe = { 
                Toast.makeText(context, "Subscription feature coming soon", Toast.LENGTH_SHORT).show()
                viewModel.dismissPremiumDialog()
            }
        )
    }
}

