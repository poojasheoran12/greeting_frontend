package com.example.greeting.presentation.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.greeting.R
import com.example.greeting.presentation.auth.AuthViewModel.AuthEvent
import com.example.greeting.presentation.core.components.*
import com.example.greeting.presentation.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> onNavigateToHome()
                AuthEvent.NavigateToProfileSetup -> onNavigateToProfileSetup()
                is AuthEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AuthEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            viewModel.onGoogleSignIn(GoogleAuthProvider.getCredential(account.idToken!!, null))
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.isLoginForm) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (state.isLoginForm) "Sign in to continue" else "Sign up to start creating",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            PremiumTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            PremiumTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            if (state.isLoginForm) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { viewModel.onForgotPassword(state.email) }) {
                        Text("Forgot Password?", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (!state.isLoginForm) {
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChange(it) },
                    label = "Confirm Password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = if (state.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                            Icon(
                                if (state.confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PremiumButton(
                text = if (state.isLoginForm) "Sign In" else "Sign Up",
                onClick = { 
                    if (state.isLoginForm) {
                        viewModel.onEmailSignIn(state.email, state.password)
                    } else {
                        if (state.password == state.confirmPassword) {
                            viewModel.onEmailSignUp(state.email, state.password)
                        } else {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "OR", style = MaterialTheme.typography.labelLarge, color = Gray400)
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    googleSignInLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.toggleFormType() }) {
                Text(
                    text = if (state.isLoginForm) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            TextButton(onClick = { viewModel.onAnonymousSignIn() }) {
                Text("Continue as Guest", color = Gray500)
            }
        }
    }
}
