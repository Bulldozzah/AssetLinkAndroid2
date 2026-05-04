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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val Blue400 = Color(0xFF60A5FA)
private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Gray50  = Color(0xFFF9FAFB)
private val Gray100 = Color(0xFFF3F4F6)
private val Gray200 = Color(0xFFE5E7EB)
private val Gray500 = Color(0xFF6B7280)
private val Gray700 = Color(0xFF374151)
private val Gray900 = Color(0xFF111827)
private val Blue100 = Color(0xFFDBEAFE)

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isSignUp = state.mode == AuthUiState.Mode.SIGN_UP
    var showPassword by remember { mutableStateOf(false) }

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
            .background(
                Brush.linearGradient(colors = listOf(Gray50, Gray100))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, Gray100),
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Blue100.copy(alpha = 0.5f),
                                        Blue100.copy(alpha = 0.15f),
                                        Color.Transparent,
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AssetLinkLogo(showTagline = true)

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = if (isSignUp) "Create Account" else "Welcome Back",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray900,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isSignUp) "Sign up to get started"
                                   else "Sign in to continue to your account",
                            fontSize = 14.sp,
                            color = Gray500,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(24.dp))

                        if (isSignUp) {
                            AuthField(
                                value = state.fullName,
                                onValueChange = vm::setFullName,
                                label = "Full Name",
                            )
                            Spacer(Modifier.height(12.dp))
                            AuthField(
                                value = state.phone,
                                onValueChange = vm::setPhone,
                                label = "Phone (optional)",
                                keyboardType = KeyboardType.Phone,
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        AuthField(
                            value = state.email,
                            onValueChange = vm::setEmail,
                            label = "Email",
                            keyboardType = KeyboardType.Email,
                        )

                        Spacer(Modifier.height(12.dp))

                        if (!isSignUp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Password",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Gray700,
                                )
                                Text(
                                    "Forgot password?",
                                    fontSize = 12.sp,
                                    color = Blue500,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = vm::setPassword,
                            label = if (isSignUp) ({ Text("Password") }) else null,
                            placeholder = { Text("••••••••", color = Color(0xFF9CA3AF)) },
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
                                        color = Blue500,
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Gray50,
                                focusedContainerColor = Gray50,
                                unfocusedBorderColor = Gray200,
                                focusedBorderColor = Blue500,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Blue400, Blue500, Blue600)
                                    )
                                )
                                .then(
                                    if (!state.loading)
                                        Modifier.clickable { vm.submit(onAuthenticated) }
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
                                    if (isSignUp) "Create Account" else "Sign In",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                )
                            }
                        }

                        if (!isSignUp) {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Gray200,
                                )
                                Text(
                                    "  or continue with  ",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF),
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Gray200,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                SocialButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Google",
                                    iconRes = R.drawable.ic_google,
                                )
                                SocialButton(
                                    modifier = Modifier.weight(1f),
                                    label = "GitHub",
                                    iconRes = R.drawable.ic_github,
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                if (isSignUp) "Already have an account? "
                                else "Don't have an account? ",
                                fontSize = 14.sp,
                                color = Gray500,
                            )
                            Text(
                                if (isSignUp) "Sign in" else "Sign up",
                                fontSize = 14.sp,
                                color = Blue500,
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
                    }
                }
            }
        }
    }
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
            unfocusedContainerColor = Gray50,
            focusedContainerColor = Gray50,
            unfocusedBorderColor = Gray200,
            focusedBorderColor = Blue500,
        ),
        shape = RoundedCornerShape(12.dp),
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
            contentColor = Gray700,
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
