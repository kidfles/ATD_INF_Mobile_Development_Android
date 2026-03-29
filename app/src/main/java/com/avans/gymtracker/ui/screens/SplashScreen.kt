package com.avans.gymtracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash screen met fade + scale animatie.
 * Na 2 seconden wordt onNavigateNext aangeroepen.
 */
@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "GymTracker",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Track your fitness journey",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}
