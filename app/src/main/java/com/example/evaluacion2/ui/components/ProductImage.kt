package com.example.evaluacion2.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.evaluacion2.R
import com.example.evaluacion2.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ProductImage(
    product: Product,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val drawableRes = bundledDrawableFor(product)
    if (drawableRes != null) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = "Imagen de ${product.name}",
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    val context = LocalContext.current
    val imageUri = product.imageUri
    val customBitmap by produceState<ImageBitmap?>(initialValue = null, imageUri) {
        value = imageUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap!!,
            contentDescription = "Imagen de ${product.name}",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Producto sin imagen",
                modifier = Modifier.fillMaxSize(0.28f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@DrawableRes
private fun bundledDrawableFor(product: Product): Int? {
    val imageKey = product.imageUri
        ?.takeIf { it.startsWith(DRAWABLE_PREFIX) }
        ?.removePrefix(DRAWABLE_PREFIX)
        ?: product.imageUri?.takeIf { it.isNotBlank() }?.let { return null }
        ?: defaultImageKeyFor(product.name)

    return when (imageKey) {
        "arco_de_ceremonia" -> R.drawable.arco_de_ceremonia
        "atrapasuenos" -> R.drawable.atrapasuenos
        "centro_de_mesa" -> R.drawable.centro_de_mesa
        "colgante_textil_festival" -> R.drawable.colgante_textil_festival
        "cortina_decorativa" -> R.drawable.cortina_decorativa
        "macetero_colgante" -> R.drawable.macetero_colgante
        "tapiz_mural" -> R.drawable.tapiz_mural
        "tapiz_mural_sol_andino" -> R.drawable.tapiz_mural_sol_andino
        else -> null
    }
}

private fun defaultImageKeyFor(productName: String): String? = when (productName.trim().lowercase(Locale.ROOT)) {
    "arco de ceremonia" -> "arco_de_ceremonia"
    "atrapasueños" -> "atrapasuenos"
    "centro de mesa" -> "centro_de_mesa"
    "colgante textil festival" -> "colgante_textil_festival"
    "cortina decorativa" -> "cortina_decorativa"
    "colgante de plantas", "macetero colgante" -> "macetero_colgante"
    "tapiz mural" -> "tapiz_mural"
    "tapiz mural sol andino" -> "tapiz_mural_sol_andino"
    else -> null
}

private const val DRAWABLE_PREFIX = "drawable:"
