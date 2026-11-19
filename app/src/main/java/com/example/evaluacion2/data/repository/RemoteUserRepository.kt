package com.example.evaluacion2.data.repository

import com.example.evaluacion2.data.local.UserCache
import com.example.evaluacion2.data.remote.api.UsuariosApi
import com.example.evaluacion2.data.remote.dto.LoginRequestDto
import com.example.evaluacion2.data.remote.dto.RegisterRequestDto
import com.example.evaluacion2.data.remote.dto.UpdateProfileRequestDto
import com.example.evaluacion2.data.remote.safeApiCall
import com.example.evaluacion2.data.remote.toDomain
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Usuarios desde ms-usuarios. El perfil se copia a SQLite para usarlo sin conexión. */
class RemoteUserRepository(
    private val api: UsuariosApi,
    private val cache: UserCache,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    override suspend fun login(email: String, password: String): User {
        val user = safeApiCall { api.login(LoginRequestDto(email.trim(), password)) }.toDomain()
        withContext(io) { cache.upsert(user) }
        return user
    }

    override suspend fun register(user: User): User {
        val body = RegisterRequestDto(user.name.trim(), user.alias.trim(), user.email.trim(), user.phone.trim(), user.password, user.avatarUri)
        val created = safeApiCall { api.register(body) }.toDomain()
        withContext(io) { cache.upsert(created) }
        return created
    }

    override suspend fun getUser(userId: Int): User? = try {
        safeApiCall { api.getUser(userId) }.toDomain().also { withContext(io) { cache.upsert(it) } }
    } catch (e: AppException.Network) {
        withContext(io) { cache.getById(userId) } ?: throw e
    }

    override suspend fun updateProfile(userId: Int, name: String, alias: String, phone: String): User {
        val current = withContext(io) { cache.getById(userId) }
        val body = UpdateProfileRequestDto(name.trim(), alias.trim(), phone.trim(), current?.avatarUri)
        val updated = safeApiCall { api.updateProfile(userId, body) }.toDomain()
        withContext(io) { cache.upsert(updated) }
        return updated
    }
}
