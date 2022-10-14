package one.devos.vrbadge

import com.mongodb.ConnectionString
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO as CIOClient
import io.ktor.server.engine.*
import io.ktor.server.cio.CIO as CIOServer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*import one.devos.vrbadge.plugins.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

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
    val customMessage: String
)


val client = KMongo.createClient(ConnectionString(System.getenv("MONGO_URI"))).coroutine
val database = client.getDatabase(System.getenv("MONGO_DATABASE"))
val col = database.getCollection<VRStatus>()

val host: String = System.getenv("HOST") ?: "0.0.0.0"
val port: Int = System.getenv("PORT")?.toInt() ?: 8080
val callback_url: String = System.getenv("CALLBACK_URL") ?: ""

fun main() {
    embeddedServer(CIOServer, port, host) {
        configureSecurity()
        configureMonitoring()
        configureSerialization()
        configureSockets()
        configureRouting()
        crab()
    }.start(wait = true)
}
