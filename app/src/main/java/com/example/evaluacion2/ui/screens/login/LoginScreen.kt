
package com.example.evaluacion2.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evaluacion2.R
import com.example.evaluacion2.ui.theme.Terracotta

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fotografía de fondo
        Image(
            painter = painterResource(
                id = R.drawable.login_background
            ),
            contentDescription = "Interior artesanal de Lou Araya Macramé",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Oscurecemos la fotografía para mejorar la legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 28.dp,
                    vertical = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))


            Image(
                painter = painterResource(
                    id = R.drawable.logo_lou_araya
                ),
                contentDescription = "Logo Lou Araya Macramé",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )


            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Arte en fibras que decora tus historias",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text("Diseños artesanales únicos · Hogares, eventos y más · Ferias y showrooms · Diseños personalizados", color = Color.White, textAlign = TextAlign.Center, fontSize = 13.sp)

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(14.dp))

            // Botón principal
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón secundario
            OutlinedButton(
                onClick = onCreateAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Regístrate",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
