package com.example.evaluacion2.data.remote

import com.example.evaluacion2.data.remote.dto.CampaignDto
import com.example.evaluacion2.data.remote.dto.ProductDto
import com.example.evaluacion2.data.remote.dto.QuoteRequestDto
import com.example.evaluacion2.data.remote.dto.UserDto
import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.model.User

fun UserDto.toDomain() = User(id = id, name = name, alias = alias, email = email, phone = phone, role = role, avatarUri = avatarUri)

fun ProductDto.toDomain() = Product(
    id = id ?: 0,
    name = name,
    description = description.orEmpty(),
    dimensions = dimensions.orEmpty(),
    materials = materials.orEmpty(),
    category = category,
    price = price,
    available = available ?: true,
    imageUri = imageUri,
    collection = collection
)

fun Product.toDto() = ProductDto(
    id = id.takeIf { it > 0 },
    name = name,
    description = description,
    dimensions = dimensions,
    materials = materials,
    category = category,
    price = price,
    available = available,
    imageUri = imageUri,
    collection = collection
)

fun CampaignDto.toDomain() = Campaign(
    id = id ?: 0,
    title = title,
    description = description.orEmpty(),
    startDate = startDate,
    endDate = endDate,
    active = active ?: true
)

fun Campaign.toDto() = CampaignDto(
    id = id.takeIf { it > 0 },
    title = title,
    description = description,
    startDate = startDate,
    endDate = endDate,
    active = active
)

fun QuoteRequestDto.toDomain() = QuoteRequest(
    id = id ?: 0,
    userId = userId,
    userName = userName.orEmpty(),
    productId = productId,
    productName = productName,
    eventType = eventType,
    dimensions = dimensions,
    material = material,
    colors = colors,
    requiredDate = requiredDate,
    observations = observations.orEmpty(),
    status = status ?: QuoteRequest.STATUS_PENDING,
    createdAt = createdAt.orEmpty()
)

fun QuoteRequest.toDto() = QuoteRequestDto(
    userId = userId,
    userName = userName,
    productId = productId,
    productName = productName,
    eventType = eventType,
    dimensions = dimensions,
    material = material,
    colors = colors,
    requiredDate = requiredDate,
    observations = observations
)
