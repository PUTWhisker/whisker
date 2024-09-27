package io.grpc.authentication

import android.net.Uri
import io.grpc.ManagedChannelBuilder
import io.grpc.soundtransfer.SoundServiceGrpcKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable

class AuthenticationGrpc(uri: Uri) : Closeable {
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
    private  val transferer = SoundServiceGrpcKt.SoundServiceCoroutineStub(channel)


    override fun close() {
        channel.shutdownNow()
    }



//    suspend fun Login(user : UserCredits)  = LoginResponse{
//        JWT = "dwa",
//        successful = true
//    }
}


