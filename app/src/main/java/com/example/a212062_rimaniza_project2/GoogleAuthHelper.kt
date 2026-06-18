package com.example.a212062_rimaniza_project2

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class GoogleAuthHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(): Boolean {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(googleIdToken).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
