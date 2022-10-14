package one.devos.vrbadge.plugins

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import one.devos.vrbadge.*
import org.litote.kmongo.eq

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    authentication {
        oauth("auth-oauth-discord") {
            urlProvider = { callback_url }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl =  "https://discord.com/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("DISCORD_CLIENT_ID"),
                    clientSecret = System.getenv("DISCORD_CLIENT_SECRET"),
                    defaultScopes = listOf("identify")
                )
            }

            client = httpClient
        }
    }

    routing {
        authenticate("auth-oauth-discord") {
            get("login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()

                val discord: DiscordUser = httpClient.get("https://discord.com/api/v9/users/@me") {
                    bearerAuth(principal?.accessToken.toString())
                }.body()

                call.sessions.set(UserSession(
                    accessToken = principal?.accessToken.toString(),
                    id = discord.id,
                    username = discord.username
                ))

                col.findOne(VRStatus::id eq discord.id) ?: col.insertOne(VRStatus(
                    id = discord.id,
                    isInVR = false,
                    isInVRGame = false,
                    startedVRAt = 0,
                    customMessage = ""
                ))

                call.respondRedirect("/me")
            }
        }

        get("/me") {
            val userSession = call.sessions.get<UserSession>() ?: return@get call.respondRedirect("/login")

            val status = col.findOne(VRStatus::id eq userSession.id) ?: return@get call.respondRedirect("/login")

            call.respondText("Hello ${userSession.username}\nIn VR? ${status.isInVR}")
        }
    }
}

class UserSession(
    val accessToken: String,
    val id: String,
    val username: String,
)

@Serializable
data class DiscordUser(
    val id: String,
    val username: String,
)