package com.example.greeting.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.greeting.domain.model.Template

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPreview: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToPreview -> {
                    onNavigateToPreview(event.templateId)
                }
                is HomeUiEvent.ShowPremiumDialog -> {
                    // Handled by state below, but could trigger extra logic here
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Greeting Designs", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                state.groupedTemplates.forEach { (category, templates) ->
                    item {
                        CategorySection(
                            title = category,
                            templates = templates,
                            onTemplateClick = { viewModel.onTemplateClick(it) }
                        )
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (state.showPremiumDialog) {
        PremiumUpsellDialog(
            onDismiss = { viewModel.dismissPremiumDialog() },
            onSubscribe = {
                Toast.makeText(context, "Subscription feature coming soon", Toast.LENGTH_SHORT).show()
                viewModel.dismissPremiumDialog()
            }
        )
    }
}

@Composable
fun CategorySection(
    title: String,
    templates: List<Template>,
    onTemplateClick: (Template) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateClick(template) }
                )
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: Template,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .aspectRatio(0.7f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = template.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            

            val badgeColor = if (template.isPremium) Color(0xFFFFD700) else Color(0xFFEEEEEE)
            val badgeText = if (template.isPremium) "PRO" else "FREE"
            val badgeTextColor = if (template.isPremium) Color.Black else Color.DarkGray
            
            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopEnd),
                color = badgeColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (template.isPremium) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeTextColor
                    )
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                        )
                    )
            )
        }
    }
}

@Composable
fun PremiumUpsellDialog(
    onDismiss: () -> Unit,
    onSubscribe: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock Premium", fontWeight = FontWeight.Bold)
            }
        },
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Get instant access to this and hundreds of other exclusive designs.")
                
                BenefitItem("Unlimited premium templates")
                BenefitItem("High-resolution exports")
                BenefitItem("Exclusive seasonal greetings")
                BenefitItem("Remove all advertisements")
            }
        },
        confirmButton = {
            Button(
                onClick = onSubscribe,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Subscribe Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.CheckCircle, 
            contentDescription = null, 
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
