package com.marrow.companion.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Teal       = Color(0xFF4DC8D4)
private val TealDark   = Color(0xFF00ACC1)
private val TealLight  = Color(0xFFE0F7FA)
private val ErrorRed   = Color(0xFFEF5350)

// Hardcoded demo credentials
private const val DEMO_EMAIL    = "demo@marrow.com"
private const val DEMO_PASSWORD = "marrow123"

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var showPassword  by remember { mutableStateOf(false) }
    var errorMsg      by remember { mutableStateOf<String?>(null) }
    var isLoading     by remember { mutableStateOf(false) }
    var visible       by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scope        = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Reset all per-session tips on each login screen visit
        com.marrow.companion.ui.screens.quiz.FeatureTipState.explanationTipShown = false
        delay(100)
        visible = true
    }

    fun attemptLogin() {
        focusManager.clearFocus()
        when {
            email.isBlank()    -> errorMsg = "Please enter your email"
            password.isBlank() -> errorMsg = "Please enter your password"
            email.trim().lowercase() != DEMO_EMAIL || password != DEMO_PASSWORD -> {
                errorMsg = "Invalid credentials. Use demo@marrow.com / marrow123"
            }
            else -> {
                errorMsg = null
                isLoading = true
                scope.launch {
                    delay(1200)   // simulate network
                    isLoading = false
                    onLoginSuccess()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Teal gradient top half ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    Brush.verticalGradient(listOf(TealDark, Teal))
                )
        )

        // ── White card bottom half ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFF5F5F5))
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(48.dp))

                // ── Logo area ──────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "M",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Teal
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "MARROW",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    "Study Companion",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(40.dp))

                // ── Login card ─────────────────────────────────────────────────
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Welcome back",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            "Sign in to continue your preparation",
                            fontSize = 13.sp,
                            color = Color(0xFF888888)
                        )

                        Spacer(Modifier.height(4.dp))

                        // Email field
                        OutlinedTextField(
                            value         = email,
                            onValueChange = { email = it; errorMsg = null },
                            label         = { Text("Email") },
                            leadingIcon   = {
                                Icon(Icons.Filled.Email, null,
                                    tint = if (email.isNotEmpty()) Teal else Color(0xFFAAAAAA))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            modifier   = Modifier.fillMaxWidth(),
                            shape      = RoundedCornerShape(12.dp),
                            colors     = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Teal,
                                focusedLabelColor    = Teal,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        // Password field
                        OutlinedTextField(
                            value         = password,
                            onValueChange = { password = it; errorMsg = null },
                            label         = { Text("Password") },
                            leadingIcon   = {
                                Icon(Icons.Filled.Lock, null,
                                    tint = if (password.isNotEmpty()) Teal else Color(0xFFAAAAAA))
                            },
                            trailingIcon  = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Filled.VisibilityOff
                                        else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = Color(0xFFAAAAAA)
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { attemptLogin() }
                            ),
                            singleLine = true,
                            modifier   = Modifier.fillMaxWidth(),
                            shape      = RoundedCornerShape(12.dp),
                            colors     = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Teal,
                                focusedLabelColor    = Teal,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        // Error message
                        AnimatedVisibility(visible = errorMsg != null) {
                            Text(
                                errorMsg ?: "",
                                color    = ErrorRed,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Login button
                        Button(
                            onClick  = { attemptLogin() },
                            enabled  = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Teal,
                                disabledContainerColor = Teal.copy(alpha = 0.6f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color  = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Sign In",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Demo credentials hint ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealLight)
                        .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable {
                            email    = DEMO_EMAIL
                            password = DEMO_PASSWORD
                            errorMsg = null
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Demo Credentials (tap to fill)",
                            fontSize   = 11.sp,
                            color      = TealDark,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "demo@marrow.com  ·  marrow123",
                            fontSize = 12.sp,
                            color    = Color(0xFF555555),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    "For NEET PG Aspirants",
                    fontSize = 11.sp,
                    color    = Color(0xFFAAAAAA),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
