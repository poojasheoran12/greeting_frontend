package com.example.greeting.presentation.preview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
                .padding(padding)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            val template = uiState.template
            val userProfile = uiState.userProfile

            if (template != null && userProfile != null) {
                GreetingPreview(
                    template = template,
                    userProfile = userProfile,
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .aspectRatio(9f / 16f)
                        .shadow(12.dp, RoundedCornerShape(8.dp))
                        .background(Color.White)
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
    BoxWithConstraints(modifier = modifier) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(containerHeight * 0.12f)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile.name,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }


            AsyncImage(
                model = template.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
        }


        val profileSize = containerWidth * 0.25f
        
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = (containerHeight * 0.12f) - (profileSize / 2))
                .size(profileSize)
                .border(
                    width = (profileSize.value * 0.08f).dp,
                    color = Color(0xFF4CAF50),
                    shape = CircleShape
                )
                .padding((profileSize.value * 0.03f).dp)
                .clip(CircleShape)
                .background(Color.White),
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
    }
}
