package one.devos.vrbadge

import com.mongodb.ConnectionString
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO as CIOClient
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.CIO as CIOServer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*import one.devos.vrbadge.plugins.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.time.LocalTime

val httpClient = HttpClient(CIOClient) {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

@Serializable
data class VRStatus(
    val id: String,
    val isInVR: Boolean,
    val isInVRGame: Boolean,
    val startedVRAt: Long,
    val customMessage: String,
)


val client = KMongo.createClient(ConnectionString("mongodb://storm:pancake@localhost:27017")).coroutine
val database = client.getDatabase("vrbadge")
val col = database.getCollection<VRStatus>()

fun main() {
    embeddedServer(CIOServer, port = 8080, host = "0.0.0.0") {
        configureSecurity()
        configureMonitoring()
        configureSerialization()
        configureSockets()
        configureRouting()
    }.start(wait = true)
}
