package io.grpc.authentication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.protobuf.Timestamp
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.soundtransfer.SpeakerAndLine
import jWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.io.Closeable
import java.time.Instant

class TranscriptionElement(val text: String, val timestamp: Instant, val id: Int, val language: String) {
    override fun toString(): String {
        return "TranscriptionElement(text='$text', timestamp=$timestamp)"
    }
}
fun createMetadata(): Metadata{
    val metadata = Metadata()
    val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
    metadata.put(key, jWT)
    return metadata
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
        channel.shutdown()
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
    suspend fun getTranscriptions(timeFrom : Timestamp? = null, timeTo : Timestamp? = null, limit: Int  = 0, language: String = ""): List<TranscriptionElement> {
        val metadata = Metadata()
        val key = Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(key, jWT)
        val query = queryParamethers{
            if (timeFrom != null) {
                this.startTime = timeFrom
            }
            if (timeTo != null){
                this.endTime = timeTo
            }
            this.limit = limit
            this.language = language
        }
        return stub.getTranscription(query, metadata).map {
            Log.d("DEBUG", "Received timestamp: seconds=${it.createdAt.seconds}, nanos=${it.createdAt.nanos}")
            TranscriptionElement(
                it.transcription,
                Instant.ofEpochSecond(it.createdAt.seconds, it.createdAt.nanos.toLong()),
                it.id,
                it.language
            )
        }.toList()
    }



    suspend fun editTranscription(id : Int, newContent : String){
        val metadata = createMetadata()
        val request = newTranscription{
            this.id = id
            this.content = newContent
        }
        stub.editTranscription(request, metadata)
    }

    suspend fun deleteTranscription(id : Int) {
        val metadata = createMetadata()
        val request = id{
            this.id = id
        }
        stub.deleteTranscription(request, metadata)
    }



    suspend fun getTranslation(timeFrom : Timestamp? = null, timeTo : Timestamp? = null, limit: Int  = 0, transcriptionLanguage: String = "", translationLanguage: String = "") :List<TranslationHistory>{
        val metadata = createMetadata()
        val request =  queryParamethers{
            if (timeFrom != null) {
                this.startTime = timeFrom
            }
            if (timeTo != null){
                this.endTime = timeTo
            }
            this.limit = limit
            this.language = transcriptionLanguage
            this.translationLanguage = translationLanguage
        }
        return stub.getTranslation(request, metadata).toList()
    }


    // jak nie edytujesz transckrypcji to ustawiasz wartości stringów na nulle
    suspend fun editTranslation(id : Int, newTranscription : String? = null, newTranslation : String? = null){
        val metadata = createMetadata()
        val request = newTranslation{
            this.id = id
            this.editTranslation = false
            this.editTranscription = false
            if (newTranscription != null) {
                this.editTranscription = true
                this.transcription = newTranscription
            }
            if (newTranslation != null){
                this.editTranslation = true
                this.translation = newTranslation
            }
        }
        stub.editTranslation(request, metadata)
    }
    suspend fun addOrEditTranslation(id : Int, content: String, lang: String){
        val metadata = createMetadata()
        val request = translationText {
            this.transcriptionId = id
            this.content = content
            this.language = lang
        }
        stub.saveOnlyTranslation(request, metadata)
    }


    suspend fun deleteTranslation(id : Int) {
        val metadata = createMetadata()
        val request = id{
            this.id = id
        }
        stub.deleteTranslation(request, metadata)
    }

    suspend fun getDiarization(timeFrom : Timestamp? = null, timeTo : Timestamp? = null, limit: Int  = 0, language: String = ""): List<DiarizationHistory>{
        val metadata = createMetadata()
        val request =  queryParamethers{
            if (timeFrom != null) {
                this.startTime = timeFrom
            }
            if (timeTo != null){
                this.endTime = timeTo
            }
            this.limit = limit
            this.language = language
        }
        return stub.getDiarization(request, metadata).toList()
    }

    suspend fun editDiarization(id: Int, line: List<String>, speaker: List<String>){
        val metadata = createMetadata()

        val builder = NewDiarization.newBuilder().
            setId(id)
        for (elem in line){
            builder.addLine(elem)
        }
        for (elem in speaker){
            builder.addSpeaker(elem)
        }
        stub.editDiarization(builder.build(), metadata)
    }

    suspend fun deleteDiarization(id: Int){
        val metadata = createMetadata()
        val request = id{
            this.id = id
        }
        stub.deleteDiarization(request, metadata)
    }
    fun Logout() {
        Log.i("auth", "Logging out")
        jWT = ""
    }
}


