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


class SpeakerAndLine(val speaker : String, val line: String){}



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


    //zmienic funkcje tak zeby zwracala wszystko
    suspend fun transcribeFile(filePath: String, language : String): SoundResponse {
            val metadata = Metadata()
            val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
            if (jWT != "") {
                metadata.put(key, jWT)
            }
            val bytes = File(filePath).readBytes().toByteString()
            val request = transcriptionRequest { this.soundData = bytes; this.sourceLanguage = language}
            return stub.transcribeFile(request, metadata)

    }

    // zamiast stringa obiekt ktory zwraca bula :3 i stringa
    fun transcribeLive(callback: (String) -> Unit) {
        val metadata = Metadata()
        val key = Metadata.Key.of("language", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(key, "pl")
        audiStreamManager.initAudioRecorder()
        audiStreamManager.record()

        CoroutineScope(Dispatchers.Default).launch {
            val requests = audiStreamManager.record()
            val builder = StringBuilder() // Bieżąca transkrypcja
            var wasPreviousChunkNew = false // Czy poprzedni chunk był "true"

            stub.transcribeLive(requests, metadata).collect { response ->
                Log.i("stream", "Got message: \"${response.text}\"")

                if (response.newChunk) {
                    if (wasPreviousChunkNew) {
                        // Ignoruj, ponieważ kolejny pusty "newChunk == true" oznacza, że nic nie jest mówione
                        Log.i("stream", "Ignoring consecutive newChunk == true with no text.")
                        return@collect
                    }
                    wasPreviousChunkNew = true // Ustaw flagę, że aktualny jest "true"

                    // Dodaj nową linię tylko wtedy, gdy faktycznie jest coś mówione
                    if (response.text.isNotEmpty()) {
                        builder.append("\n")
                    }
                } else {
                    wasPreviousChunkNew = false // Zresetuj flagę, gdy chunk nie jest nowy
                }

                // Aktualizuj treść ostatniej linii tylko wtedy, gdy tekst nie jest pusty
                if (response.text.isNotEmpty()) {
                    val lines = builder.lines()
                    if (lines.isNotEmpty()) {
                        val updated = lines.dropLast(1) + response.text
                        builder.clear()
                        builder.append(updated.joinToString("\n"))
                    } else {
                        builder.append(response.text)
                    }
                }

                callback(builder.toString())
            }
        }
    }

    //file path zawiera plik dźwiękowy
    fun translate(filePath: String, sourceLanguage : String, translationLanguage: String): Flow<SoundResponse> {
        val bytes = File(filePath).readBytes().toByteString()
        val request = translationRequest {
            this.soundData = bytes
            this.sourceLanguage = sourceLanguage
            this.translationLanguage = translationLanguage
        }
        return stub.translateFile(request)
    }


    suspend fun translateText(textToTranslate : String, textLanguage : String, tranlationLanguage : String): TextMessage{
        val request = textAndId {
            this.text = textToTranslate
            this.textLanguage = textLanguage
            this.translationLanguage = tranlationLanguage
        }
        return stub.translateText(request)
    }


    suspend fun diarizateSpeakers(filePath: String, language: String): List<SpeakerAndLine> {
        val bytes = File(filePath).readBytes().toByteString()
        Log.d("SoundTransferClient", "File content read. Byte size: ${bytes.size()}")

        val request = transcriptionRequest {
            this.soundData = bytes
            this.sourceLanguage = language
        }

        try {
            val response = stub.diarizateFile(request)
            Log.d("SoundTransferClient", "Server response: Speaker names: ${response.speakerNameList}, Texts: ${response.textList}")

            val out = mutableListOf<SpeakerAndLine>()
            for (i in response.speakerNameList.indices) {
                out.add(SpeakerAndLine(response.speakerNameList[i], response.textList[i]))
            }
            return out.toList()
        } catch (e: Exception) {
            Log.e("SoundTransferClient", "Error during server request", e)
            throw e
        }
//        return  return listOf(
//            SpeakerAndLine("Speaker 1", "Hej, jak się masz?"),
//            SpeakerAndLine("Speaker 2", "Bardzo dobrze, a ty?"),
//            SpeakerAndLine("Speaker 1", "Świetnie, dzięki!")
//        )
    }

    fun stopStream() {
        audiStreamManager.stop()
    }

    override fun close() {
        channel.shutdown()
    }
}