package io.grpc.soundtransfer

import android.net.Uri
import android.util.Log
import com.google.protobuf.kotlin.toByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import jWT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.Closeable
import java.io.File


class SpeakerAndLine(speaker : String, line: String){}


class SoundTransferClient(uri: Uri) : Closeable {
    private val audiStreamManager: AudioStreamManager = AudioStreamManager()
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
    private val stub = SoundServiceGrpcKt.SoundServiceCoroutineStub(channel)

    suspend fun transcribeFile(filePath: String, language : String): String? {
        try {
            val metadata = Metadata()
            val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
            if (jWT != "") {
                metadata.put(key, jWT)
            }
            val bytes = File(filePath).readBytes().toByteString()
            val request = transcriptionRequest { this.soundData = bytes; this.sourceLanguage = language}
            val response = stub.transcribeFile(request, metadata)
            return response.text
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun transcribeLive() {
        val metadata = Metadata()
        val key = Metadata.Key.of("language", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(key, "pl")
        audiStreamManager.initAudioRecorder()
        audiStreamManager.record()
        CoroutineScope(Dispatchers.Default).launch {
            val requests = audiStreamManager.record()
            stub.transcribeLive(requests, metadata).collect { response ->
                Log.i("stream", "Got message: \"${response.text}\"")
            }
        }
    }

    fun translate(filePath: String, sourceLanguage : String, translationLanguage: String): Flow<SoundResponse> {
        val bytes = File(filePath).readBytes().toByteString()
        val request = translationRequest {
            this.soundData = bytes
            this.sourceLanguage = sourceLanguage
            this.translationLanguage = translationLanguage
        }
        return stub.translateFile(request)
    }

    suspend fun diarizateSpeakers(filePath: String, language : String): List<SpeakerAndLine> {
        val bytes = File(filePath).readBytes().toByteString()
        val request = transcriptionRequest {
            this.soundData = bytes
            this.sourceLanguage = sourceLanguage
        }

        val response = stub.diarizateFile(request)
        val out = mutableListOf<SpeakerAndLine>()
        for (i in 0..<response.speakerNameList.size) {
            out.add(SpeakerAndLine(response.speakerNameList[i], response.textList[i]))
        }
        return out.toList()
    }

    fun stopStream() {
        audiStreamManager.stop()
    }

    override fun close() {
        channel.shutdownNow()
    }

}