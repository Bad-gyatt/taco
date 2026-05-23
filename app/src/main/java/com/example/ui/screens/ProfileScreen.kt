package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.LocalStrings
import com.example.ui.MainViewModel
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, navController: NavController) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.profile, style = Typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Avatar profile
            Box(
                modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(userName.take(1).uppercase(), style = Typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(userName, style = Typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(strings.theme, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip(text = strings.system, selected = theme == "system", onClick = { viewModel.setTheme("system") }, modifier = Modifier.weight(1f))
                    ThemeChip(text = strings.light, selected = theme == "light", onClick = { viewModel.setTheme("light") }, modifier = Modifier.weight(1f))
                    ThemeChip(text = strings.dark, selected = theme == "dark", onClick = { viewModel.setTheme("dark") }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip(text = strings.lavender, selected = theme == "lavender", onClick = { viewModel.setTheme("lavender") }, modifier = Modifier.weight(1f))
                    ThemeChip(text = strings.coffeeNight, selected = theme == "coffee", onClick = { viewModel.setTheme("coffee") }, modifier = Modifier.weight(1f))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(strings.language, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(text = "English", selected = lang == "en", onClick = { viewModel.setLanguage("en") }, modifier = Modifier.weight(1f))
                ThemeChip(text = "Українська", selected = lang == "uk", onClick = { viewModel.setLanguage("uk") }, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Text(strings.logout)
            }
        }
    }
}

@Composable
fun ThemeChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(40.dp).clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = Typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
