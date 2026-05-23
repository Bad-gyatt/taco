package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.BuildConfig
import com.example.ui.theme.Typography
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onLogin: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Text("TACO", style = Typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Вхід до системи", style = Typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ваше ім'я") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { if (name.isNotBlank()) onLogin(name) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Увійти", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("або", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (isLoading) return@OutlinedButton
                val clientId = BuildConfig.WEB_CLIENT_ID
                if (clientId == "YOUR_WEB_CLIENT_ID" || clientId.isBlank() || clientId == "null") {
                    Log.e("Auth", "WEB_CLIENT_ID not configured")
                    errorMessage = "Не налаштовано WEB_CLIENT_ID в `.env` або AI Studio Secrets."
                    return@OutlinedButton
                }

                coroutineScope.launch {
                    isLoading = true
                    try {
                        var activityContext: Context = context
                        while (activityContext is android.content.ContextWrapper) {
                            if (activityContext is android.app.Activity) break
                            activityContext = activityContext.baseContext
                        }
                        val credentialManager = CredentialManager.create(activityContext)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(clientId)
                            .setAutoSelectEnabled(false)
                            .build()
                        
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(activityContext, request)
                        val credential = result.credential
                        
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val googleName = googleIdTokenCredential.displayName ?: "Google User"
                            onLogin(googleName)
                        } else {
                            Log.e("Auth", "Unexpected credential type")
                        }
                    } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                        Log.e("Auth", "Google Sign In Failed", e)
                        errorMessage = "Помилка входу: ${e.message}"
                    } catch (e: Throwable) {
                        Log.e("Auth", "Google Sign In Failed", e)
                        errorMessage = "Помилка: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Увійти через Google")
            }
        }
    }
}
