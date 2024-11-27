package io.grpc.soundtransfer

import android.net.Uri
import android.util.Log
import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.DslList
import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.kotlin.toByteStringUtf8
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import jWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class SoundTransferGrpc(uri: Uri) : Closeable {

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
    val stub = SoundServiceGrpcKt.SoundServiceCoroutineStub(channel)

    suspend fun sendSoundFile(filePath : String): String? {
        try {
            val metadata = Metadata()
            val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
            if (jWT != ""){
                metadata.put(key, jWT)
            }
            val bytes = File(filePath).readBytes().toByteString()
            val request = soundRequest { this.soundData = bytes  }
            val response = transferer.sendSoundFile(request, metadata)
            return response.text
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun streamSoundFile() {
        val requests = generateOutgoingNotes()
        val metadata = Metadata()
        val key = Metadata.Key.of("language", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(key, "pl")
        stub.streamSoundFile(requests, metadata).collect { note ->
            Log.i("stream", "Got message: \"${note.text}\"")
        }
        Log.i("stream", "Finished RouteChat")
    }

    private fun generateOutgoingNotes(): Flow<SoundRequest> = flow {
        val notes = listOf(
            SoundRequest.newBuilder()
                .setSoundData(ByteString.copyFrom(byteArrayOf(1, 2, 3)))  // Example of setting sound data (binary)
                .build(),
            SoundRequest.newBuilder()
                .setSoundData(ByteString.copyFrom(byteArrayOf(1, 2, 3)))  // Example of setting sound data (binary)
                .build()
        )
        for (request in notes) {
            Log.i("stream","Sending message \"${request.toString()}\"")
            emit(request)
            delay(500)
        }
    }

    override fun close() {
        channel.shutdownNow()
    }

}