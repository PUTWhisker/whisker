package io.grpc.authentication

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable
import io.grpc.Metadata
import jWT
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class AuthenticationClient(uri: Uri) : Closeable {
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


    private  val transfer = ClientServiceGrpcKt.ClientServiceCoroutineStub(channel)

    override fun close() {
        channel.shutdownNow()
    }
    suspend fun Login(user : String, password : String) : Boolean {
        val arg = userCredits { this.username = user; this.password = password }
        val response = transfer.login(arg)

        if (response.successful) {
            jWT = response.jwt
            return true
        }
        return false
    }


    suspend fun Register(user : String, password : String) :Boolean {
        val arg = userCredits { this.username = user; this.password = password }
        val response = transfer.register(arg)
        return response.successful
    }

    suspend fun GetTranslations(): List<String> {
        val metadata = Metadata()
        val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(key, jWT)
        return transfer.getTranslation(empty { }, metadata).map { it.transcription }.toList()

    }
}


