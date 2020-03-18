package com.example.maptry.data.model

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(

    val userId: String,
    val displayName: String
)
