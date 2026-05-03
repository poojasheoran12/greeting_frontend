package com.example.greeting.presentation.preview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    onBackClick: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PreviewEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                PreviewEvent.ShareComplete -> {
                    Toast.makeText(context, "Greeting Shared!", Toast.LENGTH_SHORT).show()
                }
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
                },
                actions = {
                    if (uiState.isSharing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.onShareClick() },
                            enabled = uiState.template != null && uiState.userProfile != null
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            val template = uiState.template
            val userProfile = uiState.userProfile

            if (template != null && userProfile != null) {
                GreetingPreview(
                    template = template,
                    userProfile = userProfile,
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .aspectRatio(9f / 16f)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                )
            } else if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun GreetingPreview(
    template: Template,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val refWidth = 1080f
    val refHeight = 1920f

    BoxWithConstraints(modifier = modifier) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        // 1. Background Template
        AsyncImage(
            model = template.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 2. User Profile Image Overlay (with Green Border as per request)
        val rawSize = if (template.photoSlot.size <= 0f) 220f else template.photoSlot.size
        val profileSize = containerWidth * (rawSize / refWidth)
        val profileX = containerWidth * (template.photoSlot.x / refWidth)
        val profileY = containerHeight * (template.photoSlot.y / refHeight)

        Box(
            modifier = Modifier
                .size(profileSize)
                .offset(x = profileX, y = profileY)
                .graphicsLayer {
                    translationX = -size.width / 2f
                    translationY = -size.height / 2f
                }
                .border(
                    width = (profileSize.value * 0.05f).dp, // Dynamic border thickness
                    color = Color(0xFF4CAF50), // Green border as seen in the image
                    shape = CircleShape
                )
                .padding((profileSize.value * 0.02f).dp) // Subtle gap
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = userProfile.photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Default.Person),
                error = rememberVectorPainter(Icons.Default.Person)
            )
        }

        // 3. User Name Overlay
        val textX = containerWidth * (template.textSlot.x / refWidth)
        val textY = containerHeight * (template.textSlot.y / refHeight)
        
        Text(
            text = userProfile.name,
            modifier = Modifier
                .offset(x = textX, y = textY)
                .graphicsLayer {
                    translationX = -size.width / 2f
                    translationY = -size.height / 2f
                },
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )
    }
}
