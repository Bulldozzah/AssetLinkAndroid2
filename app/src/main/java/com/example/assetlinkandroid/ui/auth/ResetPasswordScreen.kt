package com.example.assetlinkandroid.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.ui.common.AssetLinkLogo
import kotlinx.coroutines.launch

private val Gray50 = Color(0xFFF9FAFB)
private val Gray200 = Color(0xFFE5E7EB)
private val Gray500 = Color(0xFF6B7280)
private val Blue500 = Color(0xFF3B82F6)

@Composable
fun ResetPasswordScreen(
    authRepo: AuthRepository,
    onDone: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AssetLinkLogo(showTagline = false)
                Spacer(Modifier.height(20.dp))

                if (success) {
                    Text(
                        "Password Updated",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Your password has been reset successfully. You can now sign in with your new password.",
                        fontSize = 14.sp,
                        color = Gray500,
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Continue to Sign In")
                    }
                } else {
                    Text(
                        "Set New Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Enter your new password below.",
                        fontSize = 14.sp,
                        color = Gray500,
                    )
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Gray50,
                            focusedContainerColor = Gray50,
                            unfocusedBorderColor = Gray200,
                            focusedBorderColor = Blue500,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it; error = null },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Gray50,
                            focusedContainerColor = Gray50,
                            unfocusedBorderColor = Gray200,
                            focusedBorderColor = Blue500,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            when {
                                password.length < 6 -> error = "Password must be at least 6 characters."
                                password != confirm -> error = "Passwords do not match."
                                else -> {
                                    loading = true
                                    error = null
                                    scope.launch {
                                        val result = runCatching { authRepo.updatePassword(password) }
                                        loading = false
                                        if (result.isSuccess) {
                                            success = true
                                        } else {
                                            error = "Failed to update password. Please try again."
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text("Update Password")
                        }
                    }
                }
            }
        }
    }
}
