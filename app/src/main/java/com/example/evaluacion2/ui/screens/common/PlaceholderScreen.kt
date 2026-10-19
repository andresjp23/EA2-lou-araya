package com.example.evaluacion2.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun PlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = title,
            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )

        Text(
            text =
                "Pantalla en desarrollo",

            modifier =
                Modifier.padding(
                    top = 8.dp,
                    bottom = 20.dp
                )
        )

        Button(
            onClick =
                onBackClick
        ) {

            Text("Volver")
        }
    }
}