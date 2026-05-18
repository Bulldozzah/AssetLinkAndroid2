package com.example.assetlinkandroid.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.example.assetlinkandroid.ui.common.AssetLinkLogo
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetlinkandroid.R

private val Indigo600 = Color(0xFF4F46E5)
private val Indigo500 = Color(0xFF6366F1)
private val Gray50  = Color(0xFFF9FAFB)
private val Gray200 = Color(0xFFE5E7EB)
private val Gray300 = Color(0xFFD1D5DB)
private val Gray500 = Color(0xFF6B7280)
private val Gray800 = Color(0xFF1F2937)

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isSignUp = state.mode == AuthUiState.Mode.SIGN_UP
    var showPassword by remember { mutableStateOf(false) }

    if (state.showForgotPassword) {
        ForgotPasswordDialog(
            email = state.forgotPasswordEmail,
            onEmailChange = vm::setForgotPasswordEmail,
            loading = state.forgotPasswordLoading,
            error = state.forgotPasswordError,
            success = state.forgotPasswordSuccess,
            onSend = vm::sendPasswordReset,
            onDismiss = vm::dismissForgotPassword,
        )
    }

    if (state.registrationSuccess) {
        AlertDialog(
            onDismissRequest = { vm.dismissRegistrationSuccess() },
            title = { Text("Check your email") },
            text = {
                Text(
                    "A confirmation link has been sent to ${state.email}.\n\n" +
                    "Please verify your email before signing in."
                )
            },
            confirmButton = {
                Button(onClick = { vm.dismissRegistrationSuccess() }) {
                    Text("Go to Sign In")
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo
            AssetLinkLogo(showTagline = false)

            Spacer(Modifier.height(24.dp))

            // Title
            Text(
                text = if (isSignUp) "Create your account" else "Log in to your account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Gray800,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Toggle sign in / sign up
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    if (isSignUp) "Already have an account? " else "Don\u2019t have an account? ",
                    fontSize = 14.sp,
                    color = Gray500,
                )
                Text(
                    if (isSignUp) "Sign in" else "Sign up",
                    fontSize = 14.sp,
                    color = Indigo600,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        showPassword = false
                        vm.setMode(
                            if (isSignUp) AuthUiState.Mode.SIGN_IN
                            else AuthUiState.Mode.SIGN_UP
                        )
                    },
                )
            }

            Spacer(Modifier.height(32.dp))

            // Form fields
            if (isSignUp) {
                AuthField(
                    value = state.fullName,
                    onValueChange = vm::setFullName,
                    label = "Full Name",
                )
                Spacer(Modifier.height(16.dp))
                AuthField(
                    value = state.phone,
                    onValueChange = vm::setPhone,
                    label = "Phone (optional)",
                    keyboardType = KeyboardType.Phone,
                )
                Spacer(Modifier.height(16.dp))
            }

            AuthField(
                value = state.email,
                onValueChange = vm::setEmail,
                label = "Email",
                keyboardType = KeyboardType.Email,
            )

            Spacer(Modifier.height(16.dp))

            // Password field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray800,
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::setPassword,
                    placeholder = { Text("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022", color = Gray300) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        TextButton(
                            onClick = { showPassword = !showPassword },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(
                                if (showPassword) "Hide" else "Show",
                                fontSize = 12.sp,
                                color = Indigo600,
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = Indigo600,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Error message
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Submit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (state.loading) Indigo500 else Indigo600)
                    .then(
                        if (!state.loading) Modifier.clickable { vm.submit(onAuthenticated) }
                        else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Text(
                        if (isSignUp) "Sign up" else "Sign in",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    )
                }
            }

            // Divider — "Or continue with"
            if (!isSignUp) {
                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    HorizontalDivider(color = Gray300)
                    Text(
                        "Or continue with",
                        fontSize = 13.sp,
                        color = Gray500,
                        modifier = Modifier
                            .background(Color.White)
                            .padding(horizontal = 12.dp),
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Social buttons - full width stacked
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SocialButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Continue with Google",
                        iconRes = R.drawable.ic_google,
                    )
                    SocialButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Continue with GitHub",
                        iconRes = R.drawable.ic_github,
                    )
                }
            }

            // Forgot password
            if (!isSignUp) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Forgot password?",
                    fontSize = 14.sp,
                    color = Indigo600,
                    modifier = Modifier.clickable { vm.showForgotPassword() },
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    success: Boolean,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = {
            Text(
                if (success) "Email Sent" else "Reset Password",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                if (success) {
                    Text(
                        "A password reset link has been sent to $email.\n\n" +
                        "Please check your inbox and follow the link to reset your password."
                    )
                } else {
                    Text(
                        "Enter your email address and we'll send you a link to reset your password.",
                        fontSize = 14.sp,
                        color = Gray500,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedBorderColor = Gray200,
                            focusedBorderColor = Indigo600,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error,
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (success) {
                Button(onClick = onDismiss) { Text("OK") }
            } else {
                Button(
                    onClick = onSend,
                    enabled = !loading,
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }
            }
        },
        dismissButton = {
            if (!success) {
                TextButton(onClick = onDismiss, enabled = !loading) {
                    Text("Cancel")
                }
            }
        },
    )
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedBorderColor = Gray200,
            focusedBorderColor = Indigo600,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SocialButton(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Gray200),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Gray800,
        ),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = Color.Unspecified,
        )
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp)
    }
}
