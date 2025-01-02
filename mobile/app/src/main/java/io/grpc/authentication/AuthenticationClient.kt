package io.grpc.authentication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import jWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.io.Closeable
import java.time.Instant


class TranscriptionElement(val text: String, val timestamp: Instant) {
    override fun toString(): String {
        return "TranscriptionElement(text='$text', timestamp=$timestamp)"
    }
}

class AuthenticationClient(uri: Uri, context: Context) : Closeable {
    private val channel = let {
        Log.i("auth", "Connecting to ${uri.host}:${uri.port}")

        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        builder.executor(Dispatchers.IO.asExecutor()).build()
    }
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private  val stub = ClientServiceGrpcKt.ClientServiceCoroutineStub(channel)

    override fun close() {
        channel.shutdownNow()
    }
    suspend fun Login(user : String, password : String) : Boolean {
        val arg = userCredits { this.username = user; this.password = password }
        try {
            val response = stub.login(arg)
            jWT = response.jwt
            sharedPreferences.edit().putString("refresh_token", response.refreshToken).apply()
        }catch (e: Exception) {
            return false
        }
        return true
    }


    suspend fun Register(user : String, password : String) :Boolean {
        val arg = userCredits { this.username = user; this.password = password }
        try {
            stub.register(arg)
        }catch (e : Exception){
            return  false
        }
        return true
    }
    suspend fun <T> grpcCallWithRetry(
        call: suspend () -> T, // The gRPC call
    ): T {
        return try {
            call()
        } catch (e: Exception) {
            val refreshToken = sharedPreferences.getString("refresh_token", null)
            if (refreshToken != null) {
                refreshToken()
                call()
            } else {
                throw e
            }
        }
    }

    suspend fun refreshToken()  {
        val rToken = sharedPreferences.getString("refresh_token", null)
            ?: throw Exception("No refresh token")

        val arg = refreshTokenRequest {
            refreshToken = rToken
        }
        val response = stub.refreshToken(arg)
        jWT = response.accessToken
        sharedPreferences.edit().putString("refresh_token", response.refreshToken).apply()
    }
    suspend fun GetTranslations(): List<TranscriptionElement> {
        return grpcCallWithRetry {
            val metadata = Metadata().apply {
                val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
                put(key, jWT)
            }

            stub.getTranscription(queryParamethers {  }, metadata).map {
                TranscriptionElement(it.transcription, Instant.ofEpochSecond(it.createdAt.seconds, it.createdAt.nanos.toLong()))
            }.toList()
        }
    }
    fun Logout() {
        Log.i("auth", "Logging out")
        jWT = ""
    }
}


