package com.availelabs.tabla.users

import kotlin.uuid.Uuid

sealed interface UserAccount
value class RegisteredUser(val id: Uuid, val authMethods: List<AuthMethod>) : UserAccount
value class GuestUser(val sessionId: Uuid) : UserAccount


value class AuthMethod(
    val provider: OAuthProvider,
    val providerUserId: String
)

enum class OAuthProvider {
    GITHUB,
}