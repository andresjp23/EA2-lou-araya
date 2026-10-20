package com.example.evaluacion2.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.R
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MILLIS = 1_900L

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.82f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val logoOffset by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 36f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "logoOffset"
    )
    val messageAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 650, delayMillis = 450),
        label = "messageAlpha"
    )
    val accentWidth by animateFloatAsState(
        targetValue = if (animationStarted) 72f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 650),
        label = "accentWidth"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(SPLASH_DURATION_MILLIS)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_lou_araya_terracotta),
                contentDescription = "Logo Lou Araya Macramé",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .graphicsLayer {
                        alpha = logoAlpha
                        scaleX = logoScale
                        scaleY = logoScale
                        translationY = logoOffset
                    }
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .width(accentWidth.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .graphicsLayer { alpha = messageAlpha }
                    .background(MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Arte en fibras que decora tus historias",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(messageAlpha)
            )
        }
    }
}
