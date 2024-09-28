package io.grpc.authentication

import android.net.Uri
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable
import io.grpc.Metadata

class AuthenticationGrpc(uri: Uri) : Closeable {
    private var JWT = ""
    private val channel = let {
        println("Connecting to ${uri.host}:${uri.port}")

        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        builder.executor(Dispatchers.IO.asExecutor()).build()
    }
//    private  val transferer = SoundServiceGrpcKt.SoundServiceCoroutineStub(channel)

    private  val transfer = ClientServiceGrpcKt.ClientServiceCoroutineStub(channel)

    override fun close() {
        channel.shutdownNow()
    }



    suspend fun Login(user : String, password : String) : Boolean {
        val arg = userCredits { this.username = user; this.password = password }
        val response = transfer.login(arg)

        if (response.successful) {
            this.JWT = response.jwt
            return true
        }
        return false
    }


    suspend fun Register(user : String, password : String) :Boolean {
        val arg = userCredits { this.username = user; this.password = password }
        val response = transfer.register(arg)
        return response.successful
    }

    suspend fun GetTranslations() {
        val metadata = Metadata()
        val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(key, this.JWT)
        val arg = empty {  }
        val response = transfer.getTranslation(empty {  }, metadata)
    }
}


