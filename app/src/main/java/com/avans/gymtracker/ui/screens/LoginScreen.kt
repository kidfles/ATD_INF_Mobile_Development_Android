package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avans.gymtracker.utils.PreferencesManager

/**
 * Login/Registreer scherm.
 * Sla gebruikersgegevens op via SharedPreferences (PreferencesManager).
 * Ondersteunt portrait én landscape layout.
 */
@Composable
fun LoginScreen(prefs: PreferencesManager, onLoginSuccess: () -> Unit) {
    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp

    var name by remember { mutableStateOf(prefs.userName) }
    var email by remember { mutableStateOf(prefs.userEmail) }
    var isRegistering by remember { mutableStateOf(prefs.userName.isEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleAuth() {
        if (name.isBlank()) { errorMessage = "Vul je naam in"; return }
        if (isRegistering && email.isBlank()) { errorMessage = "Vul je e-mail in"; return }
        if (isRegistering && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Ongeldig e-mailadres"; return
        }
        if (!isRegistering && name != prefs.userName && prefs.userName.isNotEmpty()) {
            errorMessage = "Gebruiker niet gevonden. Registreer eerst."; return
        }
        prefs.userName = name.trim()
        prefs.userEmail = if (isRegistering) email.trim() else prefs.userEmail
        prefs.isLoggedIn = true
        onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color.White))
            )
    ) {
        if (isLandscape) {
            // ── Landscape: twee kolommen ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Linkerkolom: logo
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter, null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("GymTracker", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        if (isRegistering) "Maak een account aan" else "Welkom terug",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Rechterkolom: formulier
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoginForm(
                        name = name, onNameChange = { name = it },
                        email = email, onEmailChange = { email = it },
                        isRegistering = isRegistering,
                        errorMessage = errorMessage,
                        onToggleMode = { isRegistering = !isRegistering; errorMessage = null },
                        onSubmit = ::handleAuth,
                        onDemoLogin = { name = "Demo Gebruiker"; email = "demo@gymtracker.nl"; handleAuth() }
                    )
                }
            }
        } else {
            // ── Portrait: enkelvoudige kolom ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Icon(
                    Icons.Default.FitnessCenter, null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (isRegistering) "Account aanmaken" else "Welkom terug",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    if (isRegistering) "Begin je fitness reis" else "Log in om verder te gaan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LoginForm(
                    name = name, onNameChange = { name = it },
                    email = email, onEmailChange = { email = it },
                    isRegistering = isRegistering,
                    errorMessage = errorMessage,
                    onToggleMode = { isRegistering = !isRegistering; errorMessage = null },
                    onSubmit = ::handleAuth,
                    onDemoLogin = { name = "Demo Gebruiker"; email = "demo@gymtracker.nl"; handleAuth() }
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    isRegistering: Boolean,
    errorMessage: String?,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onDemoLogin: () -> Unit
) {
    OutlinedTextField(
        value = name, onValueChange = onNameChange,
        label = { Text("Naam") },
        leadingIcon = { Icon(Icons.Default.Person, null) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    if (isRegistering) {
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

    if (errorMessage != null) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(
                errorMessage,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
        Text(if (isRegistering) "Account aanmaken" else "Inloggen")
    }

    TextButton(onClick = onToggleMode, modifier = Modifier.fillMaxWidth()) {
        Text(if (isRegistering) "Al een account? Inloggen" else "Nog geen account? Registreer")
    }

    OutlinedButton(onClick = onDemoLogin, modifier = Modifier.fillMaxWidth()) {
        Text("⚡ Doorgaan als Demo Gebruiker")
    }
}
